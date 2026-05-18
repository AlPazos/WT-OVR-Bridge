package pazos.tkStrike.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "categories", uniqueConstraints = {
        @UniqueConstraint(name = "uk_category", columnNames = {"name", "gender", "subCategory"})
})
public class Category extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false)
    public String name;

    @Column(nullable = false)
    public String gender;

    @Column(nullable = false)
    public String subCategory;

    public Integer bodyLevel;
    public Integer headLevel;

    public Integer rounds;
    public Integer roundTimeMinutes;
    public Integer roundTimeSeconds;

    public Integer kyeShiTimeMinutes;
    public Integer kyeShiTimeSeconds;

    public Boolean goldenPointEnabled;
    public Integer goldenPointTimeMinutes;
    public Integer goldenPointTimeSeconds;

    public Integer differentialScore;
    public Integer maxAllowedGamJeoms;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    public LocalDateTime createdAt;

    @UpdateTimestamp
    public LocalDateTime updatedAt;

    public Category() {
    }

    public Category(String name, String gender, String subCategory) {
        this.name = name;
        this.gender = gender;
        this.subCategory = subCategory;
    }

    public static Category findByNameGenderSubcategory(String name, String gender, String subCategory) {
        return find("name = ?1 and gender = ?2 and subCategory = ?3", name, gender, subCategory).firstResult();
    }

    @Override
    public String toString() {
        return "Category{" + name + " " + gender + " " + subCategory + "}";
    }
}