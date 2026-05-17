package pazos.tkStrike.entity;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

/**
 * Clave primaria compuesta para Category
 */
@Embeddable
public class CategoryId implements Serializable {

    public String name;
    public String gender;
    public String subCategory;

    public CategoryId() {}

    public CategoryId(String name, String gender, String subCategory) {
        this.name = name;
        this.gender = gender;
        this.subCategory = subCategory;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CategoryId that = (CategoryId) o;
        return Objects.equals(name, that.name) &&
               Objects.equals(gender, that.gender) &&
               Objects.equals(subCategory, that.subCategory);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, gender, subCategory);
    }

    @Override
    public String toString() {
        return name + " " + gender + " " + subCategory;
    }
}

