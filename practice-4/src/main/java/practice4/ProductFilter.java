package practice4;

public class ProductFilter {
    private String name;
    private String category;

    private Integer minQuantity;
    private Integer maxQuantity;

    private Double minPrice;
    private Double maxPrice;

    private int page = 1;
    private int size = 10;

    public String getName() {
        return name;
    }

    public ProductFilter setName(String name) {
        this.name = name;
        return this;
    }

    public String getCategory() {
        return category;
    }

    public ProductFilter setCategory(String category) {
        this.category = category;
        return this;
    }

    public Integer getMinQuantity() {
        return minQuantity;
    }

    public ProductFilter setMinQuantity(Integer minQuantity) {
        this.minQuantity = minQuantity;
        return this;
    }

    public Integer getMaxQuantity() {
        return maxQuantity;
    }

    public ProductFilter setMaxQuantity(Integer maxQuantity) {
        this.maxQuantity = maxQuantity;
        return this;
    }

    public Double getMinPrice() {
        return minPrice;
    }

    public ProductFilter setMinPrice(Double minPrice) {
        this.minPrice = minPrice;
        return this;
    }

    public Double getMaxPrice() {
        return maxPrice;
    }

    public ProductFilter setMaxPrice(Double maxPrice) {
        this.maxPrice = maxPrice;
        return this;
    }

    public int getPage() {
        return page;
    }

    public ProductFilter setPage(int page) {
        if (page > 0) {
            this.page = page;
        }

        return this;
    }

    public int getSize() {
        return size;
    }

    public ProductFilter setSize(int size) {
        if (size > 0) {
            this.size = size;
        }

        return this;
    }

    public int getOffset() {
        return (page - 1) * size;
    }
}