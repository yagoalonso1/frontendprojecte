package com.example.app.model.evento

enum class CategoriaEvento(val valor: String) {
    ARTE_Y_CULTURA("Arte y Cultura"),
    BELLEZA_Y_BIENESTAR("Belleza y Bienestar"),
    CHARLAS_Y_CONFERENCIAS("Charlas y Conferencias"),
    CINE("Cine"),
    COMEDIA("Comedia"),
    CONCIERTOS("Conciertos"),
    DEPORTES("Deportes"),
    DISCOTECA("Discoteca"),
    EDUCACION("Educación"),
    EMPRESARIAL("Empresarial"),
    EXPOSICIONES("Exposiciones"),
    FAMILIAR("Familiar"),
    FESTIVALES("Festivales"),
    GASTRONOMIA("Gastronomía"),
    INFANTIL("Infantil"),
    MODA("Moda"),
    MUSICA_EN_DIRECTO("Música en Directo"),
    NETWORKING("Networking"),
    OCIO_NOCTURNO("Ocio Nocturno"),
    PRESENTACIONES("Presentaciones"),
    RESTAURANTES("Restaurantes"),
    SALUD_Y_DEPORTE("Salud y Deporte"),
    SEMINARIOS("Seminarios"),
    TALLERES("Talleres"),
    TEATRO("Teatro"),
    TECNOLOGIA("Tecnología"),
    TURISMO("Turismo"),
    VARIOS("Varios");

    companion object {
        fun getAllValues(): List<String> {
            return values().map { it.valor }.sorted()
        }

        fun fromString(valor: String): CategoriaEvento? {
            return values().find { it.valor == valor }
        }
    }
}