package com.crishof.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Objects;

/*
 * @Embeddable: objeto de valor que no tiene identidad propia.
 * Sus columnas se añaden directamente a la tabla de la entidad que lo contiene.
 *
 * Ventajas sobre una tabla separada:
 * - No requiere JOIN para leer la dirección
 * - Sin overhead de @OneToOne para datos que siempre van juntos
 * - El objeto tiene semántica de valor (sin id propio)
 *
 * @AttributeOverrides permite cambiar el nombre de columna cuando
 * la misma clase @Embeddable se usa más de una vez en la misma entidad.
 */
@Embeddable
public class Address {

    @Column(name = "address_street")
    private String street;

    @Column(name = "address_city")
    private String city;

    @Column(name = "address_country")
    private String country;

    @Column(name = "address_zip_code")
    private String zipCode;

    // Constructor vacío requerido por JPA
    protected Address() {
    }

    // Constructor con parámetros para crear instancias en código
    public Address(String street, String city, String country, String zipCode) {
        this.street = street;
        this.city = city;
        this.country = country;
        this.zipCode = zipCode;
    }

    // Getters (sin setters: Address es inmutable, patrón Value Object)

    public String getStreet() {
        return street;
    }

    public String getCity() {
        return city;
    }

    public String getCountry() {
        return country;
    }

    public String getZipCode() {
        return zipCode;
    }

    // equals() y hashCode() basados en todos los campos (semántica de valor)

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Address address)) return false;
        return Objects.equals(street, address.street)
                && Objects.equals(city, address.city)
                && Objects.equals(country, address.country)
                && Objects.equals(zipCode, address.zipCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(street, city, country, zipCode);
    }
}
