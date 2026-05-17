package pazos.tkStrike.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import pazos.tkStrike.entity.Category;
import pazos.tkStrike.entity.CategoryId;

/**
 * Repository para categorías usando Panache
 */
@ApplicationScoped
public class CategoryRepository implements PanacheRepository<Category> {

    /**
     * Busca una categoría por su ID compuesta
     */
    public Category findById(CategoryId id) {
        return find("id", id).firstResult();
    }

    /**
     * Busca una categoría por sus componentes
     */
    public Category findByNameGenderSubcategory(String name, String gender, String subCategory) {
        CategoryId id = new CategoryId(name, gender, subCategory);
        return findById(id);
    }
}

