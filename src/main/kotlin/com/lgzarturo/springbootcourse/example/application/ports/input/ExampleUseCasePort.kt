package com.lgzarturo.springbootcourse.example.application.ports.input

import com.lgzarturo.springbootcourse.example.adapters.rest.dto.request.ExamplePatchUpdate
import com.lgzarturo.springbootcourse.example.domain.Example
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

/**
 * Port de entrada para los casos de uso de ejemplo.
 * Define las operaciones que se pueden realizar sobre los ejemplos.
 */
interface ExampleUseCasePort {
    /**
     * Crea un nuevo ejemplo
     * @param example Objeto Example con los datos del ejemplo a crear
     * @return Example con los datos del ejemplo creado
     */
    fun create(example: Example): Example

    /**
     * Actualiza un ejemplo
     * @param id ID del ejemplo a actualizar
     * @param example Objeto Example con los datos del ejemplo a actualizar
     * @return Example con los datos del ejemplo actualizado
     */
    fun update(
        id: Long,
        example: Example,
    ): Example

    /**
     * Obtiene un ejemplo por su ID
     * @param id ID del ejemplo a obtener
     * @return Example con los datos del ejemplo obtenido
     */
    fun findById(id: Long): Example

    /**
     * Elimina un ejemplo por su ID
     * @param id ID del ejemplo a eliminar
     */
    fun delete(id: Long)

    /**
     * Obtiene una lista paginada de ejemplos, opcionalmente filtrados por texto de búsqueda
     * @param searchText Texto de búsqueda para filtrar los ejemplos (opcional)
     * @param pageable Objeto Pageable para la paginación
     * @return Página de Example con los ejemplos obtenidos
     */
    fun findAll(
        searchText: String?,
        pageable: Pageable,
    ): Page<Example>

    /**
     * Actualiza un ejemplo
     * @param id ID del ejemplo a actualizar
     * @param update Objeto ExamplePatchUpdate con los datos del ejemplo a actualizar
     * @return Example con los datos del ejemplo actualizado
     */
    fun patch(
        id: Long,
        update: ExamplePatchUpdate,
    ): Example
}
