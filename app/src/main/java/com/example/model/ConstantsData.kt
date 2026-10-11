package com.example.model

object ConstantsData {
    val ALL_CONSTANTS: List<ConstantItem> = listOf(
        // Mathematical Constants
        ConstantItem("π", "Pi", "3.141592653589793", "Mathematics", "dimensionless"),
        ConstantItem("e", "Euler's Number", "2.718281828459045", "Mathematics", "dimensionless"),
        ConstantItem("φ", "Golden Ratio (Phi)", "1.618033988749895", "Mathematics", "dimensionless"),
        ConstantItem("√2", "Pythagoras' Constant", "1.414213562373095", "Mathematics", "dimensionless"),
        ConstantItem("ln(2)", "Natural Log of 2", "0.693147180559945", "Mathematics", "dimensionless"),
        ConstantItem("ln(10)", "Natural Log of 10", "2.302585092994046", "Mathematics", "dimensionless"),

        // Physical Constants
        ConstantItem("c", "Speed of Light", "299792458", "Physics", "m/s"),
        ConstantItem("g", "Standard Gravity", "9.80665", "Physics", "m/s²"),
        ConstantItem("G", "Gravitational Constant", "6.67430e-11", "Physics", "m³/(kg·s²)"),
        ConstantItem("h", "Planck's Constant", "6.62607015e-34", "Quantum", "J·s"),
        ConstantItem("ħ", "Reduced Planck's Constant", "1.054571817e-34", "Quantum", "J·s"),
        ConstantItem("k_B", "Boltzmann Constant", "1.380649e-23", "Thermodynamics", "J/K"),
        ConstantItem("N_A", "Avogadro's Number", "6.02214076e23", "Chemistry", "mol⁻¹"),
        ConstantItem("R", "Ideal Gas Constant", "8.314462618", "Thermodynamics", "J/(mol·K)"),
        ConstantItem("e_chg", "Elementary Charge", "1.602176634e-19", "Electromagnetism", "C"),
        ConstantItem("ε₀", "Vacuum Permittivity", "8.8541878128e-12", "Electromagnetism", "F/m"),
        ConstantItem("μ₀", "Vacuum Permeability", "1.25663706212e-6", "Electromagnetism", "N/A²"),
        ConstantItem("m_e", "Electron Rest Mass", "9.1093837015e-31", "Particle Physics", "kg"),
        ConstantItem("m_p", "Proton Rest Mass", "1.67262192369e-27", "Particle Physics", "kg")
    )
}
