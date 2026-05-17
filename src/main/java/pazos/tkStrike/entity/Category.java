package pazos.tkStrike.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entidad que representa una categoría de competencia.
 * PK compuesta por: name + gender + subCategory
 * Contiene la configuración de rondas y niveles de cuerpo/cabeza
 */
@Entity
@Table(name = "categories")
public class Category {

    @EmbeddedId
    public CategoryId id;

    // Configuración de niveles
    public Integer bodyLevel;
    public Integer headLevel;

    // Configuración de rondas
    public Integer rounds;
    public Integer roundTimeMinutes;
    public Integer roundTimeSeconds;

    // Kyeshi (tiempo de descanso)
    public Integer kyeShiTimeMinutes;
    public Integer kyeShiTimeSeconds;

    // Golden point (prórroga)
    public Boolean goldenPointEnabled;
    public Integer goldenPointTimeMinutes;
    public Integer goldenPointTimeSeconds;

    // Reglas de competencia
    public Integer differentialScore; // maxDiff
    public Integer maxAllowedGamJeoms;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    public LocalDateTime createdAt;

    @UpdateTimestamp
    public LocalDateTime updatedAt;

    // Constructores
    public Category() {}

    public Category(CategoryId id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Category{" + id + "}";
    }
}

