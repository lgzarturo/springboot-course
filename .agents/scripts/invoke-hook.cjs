#!/usr/bin/env node
'use strict';

const { spawnSync } = require('node:child_process');
const { existsSync, readFileSync, realpathSync } = require('node:fs');
const { delimiter, dirname, join, resolve } = require('node:path');

const VALID_EVENTS = ['pre-tool', 'post-tool', 'session-start'];
const SPAWN_TIMEOUT = 10000;

const rawArgs = process.argv.slice(1);
const args = rawArgs.filter((arg) => {
  if (VALID_EVENTS.includes(arg) || arg.startsWith('-')) return true;
  return false;
});

const event = args.find((a) => VALID_EVENTS.includes(a)) || 'pre-tool';
const extra = args.filter((a) => a !== event);
const isAgy = process.argv.some((a) => a === '--format=agy') || extra.includes('--format=agy');
const failClosed = process.env.CC_HOOK_FAIL_CLOSED === '1' || extra.includes('--fail-closed');
const checkOnly = extra.includes('--check');
const runnerExtra = extra.filter(arg => arg !== '--fail-closed' && arg !== '--check');

function findProjectRoot() {
  const explicit = process.env.PROJECT_ROOT || process.env.CLAUDE_PROJECT_DIR || process.env.WORKSPACE_DIR;
  if (explicit) return resolve(explicit);
  let dir = process.cwd();
  while (true) {
    if (
      existsSync(join(dir, 'package.json')) ||
      existsSync(join(dir, '.git')) ||
      existsSync(join(dir, '.agents')) ||
      existsSync(join(dir, '.claude'))
    ) {
      return dir;
    }
    const parent = dirname(dir);
    if (parent === dir) break;
    dir = parent;
  }
  return process.cwd();
}

const projectRoot = findProjectRoot();
// Buffer once: each fallback attempt must receive the same host payload.
const input = process.stdin.isTTY ? '' : readFileSync(0, 'utf8');

/**
 * Run one candidate runner. Stdout/stderr are captured (not inherited) so a
 * candidate that writes partial output before failing can never leak onto
 * this process's real stdout ahead of a later candidate's output or the
 * fallback JSON — every event this process ever emits on stdout must be a
 * single clean document. Stderr is diagnostic-only and safe to relay
 * unconditionally; stdout is only ever relayed on the two branches that
 * immediately exit, so nothing else can write to stdout afterward.
 */
function tryRun(bin, runArgs) {
  try {
    const result = spawnSync(bin, checkOnly ? [...runArgs.slice(0, runArgs.indexOf('hook')), '--help'] : runArgs, {
      cwd: projectRoot,
      stdio: ['pipe', 'pipe', 'pipe'],
      input,
      windowsHide: true,
      env: process.env,
      encoding: 'utf8',
      timeout: SPAWN_TIMEOUT,
    });
    if (result.error) {
      return false;
    }
    if (result.stderr) {
      process.stderr.write(result.stderr);
    }
    if (result.status === 0) {
      if (checkOnly) {
        process.stdout.write(JSON.stringify({ operational: true, failClosed }) + '\n');
        process.exit(0);
      }
      if (result.stdout) process.stdout.write(result.stdout);
      process.exit(0);
    }
    if (!checkOnly && result.status === 2) {
      if (isAgy) process.stdout.write(JSON.stringify({ decision: 'deny', reason: 'CodeConductor runner denied the tool.' }) + '\n');
      process.exit(isAgy ? 0 : 2);
    }
    fallback();
  } catch {
    return false;
  }
}

function fallback() {
  if (checkOnly) {
    process.stdout.write(JSON.stringify({ operational: false, failClosed }) + '\n');
    process.exit(1);
  }
  const denied = event === 'pre-tool' && failClosed;
  if (denied || event === 'session-start') {
    process.stderr.write('CodeConductor guard unavailable: install cc-codeconductor locally or enable CC_DEV=1 for trusted development.\n');
  }
  if (isAgy) {
    if (event === 'post-tool' || event === 'session-start') {
      process.stdout.write('{}\n');
    } else {
      process.stdout.write(JSON.stringify(denied ? { decision: 'deny', reason: 'CodeConductor guard unavailable' } : { decision: 'allow' }) + '\n');
    }
  }
  process.exit(denied && !isAgy ? 2 : 0);
}

try {
  // spawnSync('bun', ['--version']) as a pre-check before every real attempt
  // used to cost a second subprocess on every single hook invocation. A
  // missing/unrunnable 'bun' already surfaces as `result.error` inside
  // tryRun(), so the pre-check is redundant — dropping it halves the spawn
  // count (and the worst-case latency) for the common case where bun exists.
  const srcMain = join(projectRoot, 'src', 'cli', 'main.ts');
  if (process.env.CC_DEV === '1' && existsSync(srcMain)) {
    const metadata = JSON.parse(readFileSync(join(projectRoot, 'package.json'), 'utf8'));
    if (metadata.name === 'cc-codeconductor') tryRun('bun', ['run', srcMain, 'hook', event, ...runnerExtra]);
  }

  const packaged = join(projectRoot, 'node_modules', 'cc-codeconductor', 'dist', 'index.js');
  if (existsSync(packaged)) {
    tryRun(process.execPath, [packaged, 'hook', event, ...runnerExtra]);
  }

  const localDist = join(projectRoot, 'dist', 'index.js');
  if (process.env.CC_DEV === '1' && existsSync(localDist)) {
    const metadata = JSON.parse(readFileSync(join(projectRoot, 'package.json'), 'utf8'));
    if (metadata.name === 'cc-codeconductor') tryRun(process.execPath, [localDist, 'hook', event, ...runnerExtra]);
  }

  // Locate global npm installs without invoking npx or Windows .cmd shims.
  for (const binDir of (process.env.PATH || '').split(delimiter).filter(Boolean)) {
    const candidates = [
      join(binDir, 'node_modules', 'cc-codeconductor', 'dist', 'index.js'),
      join(binDir, '..', 'lib', 'node_modules', 'cc-codeconductor', 'dist', 'index.js'),
    ];
    const executable = join(binDir, 'cc-codeconductor');
    if (existsSync(executable)) {
      const resolved = realpathSync(executable);
      if (resolved.endsWith(join('cc-codeconductor', 'dist', 'index.js'))) candidates.push(resolved);
    }
    for (const candidate of candidates) {
      if (existsSync(candidate)) tryRun(process.execPath, [candidate, 'hook', event, ...runnerExtra]);
    }
  }
} catch {
  // Ignore errors in runner attempts
}

fallback();
