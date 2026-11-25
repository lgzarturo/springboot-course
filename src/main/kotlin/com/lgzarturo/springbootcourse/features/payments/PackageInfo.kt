/**
 * Feature: Payments
 *
 * Esta feature gestiona los pagos simulados de reservas confirmadas del hotel
 *
 * Casos de uso:
 * - Cobro simulado con importe igual al total de la reserva
 * - Transición de la reserva a IN_PROGRESS al completar el pago
 * - Reembolso de pagos completados
 * - Consulta de pagos con filtros por reserva y estado
 */
@file:Suppress("unused")

package com.lgzarturo.springbootcourse.features.payments
