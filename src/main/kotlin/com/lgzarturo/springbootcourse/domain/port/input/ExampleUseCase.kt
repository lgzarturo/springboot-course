package com.lgzarturo.springbootcourse.domain.port.input

import com.lgzarturo.springbootcourse.domain.model.Example

interface ExampleUseCase {
    fun create(example: Example): Example
}
