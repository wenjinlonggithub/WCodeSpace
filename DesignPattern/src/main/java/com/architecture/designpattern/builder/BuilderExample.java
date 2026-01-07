package com.architecture.designpattern.builder;

public class BuilderExample {
    
    public void demonstratePattern() {
        System.out.println("=== 建造者模式演示 ===");
        
        // 电脑建造者演示
        System.out.println("1. 电脑建造者:");
        Computer gamingComputer = new Computer.Builder("Intel i9", "32GB")
                .setStorage("1TB SSD")
                .setGraphicsCard("RTX 4080")
                .setMotherboard("ASUS ROG")
                .setPowerSupply("850W")
                .setCase("RGB Gaming Case")
                .build();
        
        System.out.println("游戏电脑配置: " + gamingComputer);
        
        Computer officeComputer = new Computer.Builder("Intel i5", "16GB")
                .setStorage("512GB SSD")
                .setMotherboard("ASUS Prime")
                .build();
        
        System.out.println("办公电脑配置: " + officeComputer);
        
        // 房屋建造者演示
        System.out.println("\n2. 房屋建造者:");
        HouseDirector director = new HouseDirector();
        
        VillaBuilder villaBuilder = new VillaBuilder();
        House villa = director.construct(villaBuilder);
        System.out.println("别墅: " + villa);
        
        ApartmentBuilder apartmentBuilder = new ApartmentBuilder();
        House apartment = director.construct(apartmentBuilder);
        System.out.println("公寓: " + apartment);
        
        // SQL建造者演示
        System.out.println("\n3. SQL建造者:");
        String sql = new SQLBuilder()
                .select("name, age, email")
                .from("users")
                .where("age > 18")
                .and("status = 'active'")
                .orderBy("name")
                .limit(10)
                .build();
        
        System.out.println("生成的SQL: " + sql);
    }
}

// 1. 电脑建造者（链式调用风格）
class Computer {
    private final String cpu;           // 必需
    private final String memory;        // 必需
    private final String storage;       // 可选
    private final String graphicsCard;  // 可选
    private final String motherboard;   // 可选
    private final String powerSupply;   // 可选
    private final String computerCase;  // 可选
    
    private Computer(Builder builder) {
        this.cpu = builder.cpu;
        this.memory = builder.memory;
        this.storage = builder.storage;
        this.graphicsCard = builder.graphicsCard;
        this.motherboard = builder.motherboard;
        this.powerSupply = builder.powerSupply;
        this.computerCase = builder.computerCase;
    }
    
    public static class Builder {
        private final String cpu;           // 必需参数
        private final String memory;        // 必需参数
        private String storage;
        private String graphicsCard;
        private String motherboard;
        private String powerSupply;
        private String computerCase;
        
        public Builder(String cpu, String memory) {
            this.cpu = cpu;
            this.memory = memory;
        }
        
        public Builder setStorage(String storage) {
            this.storage = storage;
            return this;
        }
        
        public Builder setGraphicsCard(String graphicsCard) {
            this.graphicsCard = graphicsCard;
            return this;
        }
        
        public Builder setMotherboard(String motherboard) {
            this.motherboard = motherboard;
            return this;
        }
        
        public Builder setPowerSupply(String powerSupply) {
            this.powerSupply = powerSupply;
            return this;
        }
        
        public Builder setCase(String computerCase) {
            this.computerCase = computerCase;
            return this;
        }
        
        public Computer build() {
            return new Computer(this);
        }
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Computer{cpu='").append(cpu).append('\'');
        sb.append(", memory='").append(memory).append('\'');
        if (storage != null) sb.append(", storage='").append(storage).append('\'');
        if (graphicsCard != null) sb.append(", graphicsCard='").append(graphicsCard).append('\'');
        if (motherboard != null) sb.append(", motherboard='").append(motherboard).append('\'');
        if (powerSupply != null) sb.append(", powerSupply='").append(powerSupply).append('\'');
        if (computerCase != null) sb.append(", case='").append(computerCase).append('\'');
        sb.append('}');
        return sb.toString();
    }
}

// 2. 房屋建造者（经典GoF风格）
class House {
    private String foundation;
    private String structure;
    private String roof;
    private String interior;
    private String garden;
    
    public void setFoundation(String foundation) { this.foundation = foundation; }
    public void setStructure(String structure) { this.structure = structure; }
    public void setRoof(String roof) { this.roof = roof; }
    public void setInterior(String interior) { this.interior = interior; }
    public void setGarden(String garden) { this.garden = garden; }
    
    @Override
    public String toString() {
        return "House{" +
                "foundation='" + foundation + '\'' +
                ", structure='" + structure + '\'' +
                ", roof='" + roof + '\'' +
                ", interior='" + interior + '\'' +
                ", garden='" + garden + '\'' +
                '}';
    }
}

// 抽象建造者
abstract class HouseBuilder {
    protected House house = new House();
    
    public abstract void buildFoundation();
    public abstract void buildStructure();
    public abstract void buildRoof();
    public abstract void buildInterior();
    public abstract void buildGarden();
    
    public House getHouse() {
        return house;
    }
}

// 别墅建造者
class VillaBuilder extends HouseBuilder {
    @Override
    public void buildFoundation() {
        house.setFoundation("深层混凝土基础");
    }
    
    @Override
    public void buildStructure() {
        house.setStructure("钢筋混凝土结构");
    }
    
    @Override
    public void buildRoof() {
        house.setRoof("琉璃瓦屋顶");
    }
    
    @Override
    public void buildInterior() {
        house.setInterior("豪华装修");
    }
    
    @Override
    public void buildGarden() {
        house.setGarden("私人花园");
    }
}

// 公寓建造者
class ApartmentBuilder extends HouseBuilder {
    @Override
    public void buildFoundation() {
        house.setFoundation("标准基础");
    }
    
    @Override
    public void buildStructure() {
        house.setStructure("框架结构");
    }
    
    @Override
    public void buildRoof() {
        house.setRoof("平顶");
    }
    
    @Override
    public void buildInterior() {
        house.setInterior("简约装修");
    }
    
    @Override
    public void buildGarden() {
        house.setGarden("公共绿地");
    }
}

// 指挥者
class HouseDirector {
    public House construct(HouseBuilder builder) {
        builder.buildFoundation();
        builder.buildStructure();
        builder.buildRoof();
        builder.buildInterior();
        builder.buildGarden();
        return builder.getHouse();
    }
}

// 3. SQL建造者
class SQLBuilder {
    private StringBuilder query = new StringBuilder();
    
    public SQLBuilder select(String columns) {
        query.append("SELECT ").append(columns);
        return this;
    }
    
    public SQLBuilder from(String table) {
        query.append(" FROM ").append(table);
        return this;
    }
    
    public SQLBuilder where(String condition) {
        query.append(" WHERE ").append(condition);
        return this;
    }
    
    public SQLBuilder and(String condition) {
        query.append(" AND ").append(condition);
        return this;
    }
    
    public SQLBuilder or(String condition) {
        query.append(" OR ").append(condition);
        return this;
    }
    
    public SQLBuilder orderBy(String column) {
        query.append(" ORDER BY ").append(column);
        return this;
    }
    
    public SQLBuilder limit(int count) {
        query.append(" LIMIT ").append(count);
        return this;
    }
    
    public String build() {
        return query.toString();
    }
}