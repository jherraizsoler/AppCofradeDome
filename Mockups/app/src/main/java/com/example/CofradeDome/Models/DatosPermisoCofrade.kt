package com.example.CofradeDome.Models

class DatosPermisoCofrade {
    var idCofradePermiso: Int
    var idCofrade: Int
    var nivelPermiso: Int

    lateinit var fechaOtorgamiento: String
    var correoElectronico: String


    constructor(
        idCofradePermiso: Int,
        idCofrade: Int,
        nivelPermiso: Int,
        correoElectronico: String
    ) {
        this.idCofradePermiso = idCofradePermiso
        this.idCofrade = idCofrade
        this.nivelPermiso = nivelPermiso
        this.correoElectronico = correoElectronico
    }

    constructor(
        idCofradePermiso: Int,
        idCofrade: Int,
        nivelPermiso: Int,
        fechaOtorgamiento: String,
        correoElectronico: String
    ) {
        this.idCofradePermiso = idCofradePermiso
        this.idCofrade = idCofrade
        this.nivelPermiso = nivelPermiso
        this.fechaOtorgamiento = fechaOtorgamiento
        this.correoElectronico = correoElectronico
    }
}
