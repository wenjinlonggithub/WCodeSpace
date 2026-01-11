package com.architecture.algorithm.opensource;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Jackson库中算法应用案例
 * 展示Jackson库中使用的各种经典算法和数据结构
 */
public class JacksonAlgorithms {
    
    /**
     * 演示Jackson中的JSON解析算法 (递归下降解析)
     * 
     * 📊 算法原理：
     * - 递归下降解析是一种自顶向下的解析方法，为每个语法构造建立一个解析函数
     * - 通过递归调用解析不同层次的语法结构
     * 
     * 🔧 解析步骤：
     * 1️⃣ 词法分析 - 将输入分解为标记(tokens)
     * 2️⃣ 语法分析 - 根据JSON语法规则构建数据结构
     * 3️⃣ 递归处理 - 对象{}、数组[]、基本值分别处理
     * 
     * 🔄 算法逻辑：
     * - 检查当前字符类型 → 分派到相应解析函数
     * - 对象解析 → 解析键值对直到遇到'}'
     * - 数组解析 → 解析元素直到遇到']'
     * - 字符串/数值解析 → 提取原始值
     * 
     * 递归下降解析算法原理：
     * - 是一种自顶向下的解析方法，为每个语法构造建立一个解析函数
     * - 通过递归调用解析不同层次的语法结构
     * - 在JSON解析中，对应解析对象({})、数组([])、字符串("")、数字等不同元素
     * 
     * 背景：
     * - Jackson是一个高性能的JSON处理器，广泛应用于Java项目
     * - 递归下降解析是一种经典的编译原理技术，适用于结构清晰的文法
     * - JSON具有明确的层次结构，适合使用递归下降解析
     * 
     * 应用场景：
     * - Web API数据交换 - 解析RESTful服务返回的JSON数据
     * - 配置文件读取 - 解析JSON格式的配置
     * - 数据序列化/反序列化 - 在对象和JSON间转换
     * - 日志系统 - 解析结构化日志数据
     * - 微服务通信 - 解析服务间的JSON消息
     * 
     * Jackson中相关组件：
     * - JsonParser - 核心解析接口
     * - JsonFactory - 创建解析器实例
     * - JsonToken - 解析过程中的标记类型
     * - Tree Model (JsonNode) - 构建JSON树形结构
     */
    public void demonstrateJsonParsingAlgorithm() {
        System.out.println("1. Jackson JSON解析算法 (递归下降解析)");
        
        JsonParser parser = new JsonParser();
        
        String jsonString = "{\n" +
                           "  \"name\": \"John Doe\",\n" +
                           "  \"age\": 30,\n" +
                           "  \"address\": {\n" +
                           "    \"street\": \"123 Main St\",\n" +
                           "    \"city\": \"New York\"\n" +
                           "  },\n" +
                           "  \"phones\": [\"123-456-7890\", \"098-765-4321\"]\n" +
                           "}";
        
        System.out.println("   原始JSON字符串:");
        System.out.println(jsonString);
        
        Object parsedObject = parser.parse(jsonString);
        System.out.println("   解析结果: " + parsedObject.toString().substring(0, Math.min(100, parsedObject.toString().length())) + "...");
    }
    
    /**
     * 演示Jackson中的序列化算法
     */
    public void demonstrateSerializationAlgorithm() {
        System.out.println("\n2. Jackson序列化算法");
        
        SerializationEngine serializer = new SerializationEngine();
        
        // 创建示例对象
        Person person = new Person();
        person.setName("Alice Johnson");
        person.setAge(28);
        person.setEmail("alice@example.com");
        person.setSkills(Arrays.asList("Java", "Spring", "Microservices"));
        
        System.out.println("   序列化前对象: " + person);
        
        String serializedJson = serializer.serialize(person);
        System.out.println("   序列化后JSON: " + serializedJson);
        
        // 反序列化
        Person deserializedPerson = serializer.deserialize(serializedJson, Person.class);
        System.out.println("   反序列化后对象: " + deserializedPerson);
    }
    
    /**
     * 演示Jackson中的树模型算法
     */
    public void demonstrateTreeModelAlgorithm() {
        System.out.println("\n3. Jackson树模型算法");
        
        TreeModel treeModel = new TreeModel();
        
        // 构建JSON树
        TreeNode root = treeModel.createObjectNode();
        root.put("id", 1);
        root.put("name", "Product A");
        
        TreeNode priceNode = treeModel.createObjectNode();
        priceNode.put("amount", 29.99);
        priceNode.put("currency", "USD");
        root.set("price", priceNode);
        
        TreeNode tagsNode = treeModel.createArrayNode();
        tagsNode.add("electronics");
        tagsNode.add("gadget");
        tagsNode.add("new");
        root.set("tags", tagsNode);
        
        System.out.println("   JSON树结构:");
        System.out.println(treeModel.toJson(root));
        
        // 遍历树
        System.out.println("   树遍历结果:");
        treeModel.traverse(root, 0);
    }
    
    /**
     * 演示Jackson中的类型识别算法
     */
    public void demonstrateTypeResolutionAlgorithm() {
        System.out.println("\n4. Jackson类型识别算法");
        
        TypeResolver resolver = new TypeResolver();
        
        // 注册类型映射
        resolver.registerType("person", Person.class);
        resolver.registerType("employee", Employee.class);
        resolver.registerType("customer", Customer.class);
        
        // 模拟多态反序列化
        String personJson = "{\"type\":\"person\",\"name\":\"John\",\"age\":30}";
        String employeeJson = "{\"type\":\"employee\",\"name\":\"Jane\",\"age\":25,\"department\":\"IT\"}";
        String customerJson = "{\"type\":\"customer\",\"name\":\"Bob\",\"age\":35,\"customerId\":\"C001\"}";
        
        Object personObj = resolver.resolveAndDeserialize(personJson);
        Object employeeObj = resolver.resolveAndDeserialize(employeeJson);
        Object customerObj = resolver.resolveAndDeserialize(customerJson);
        
        System.out.println("   反序列化Person: " + personObj);
        System.out.println("   反序列化Employee: " + employeeObj);
        System.out.println("   反序列化Customer: " + customerObj);
    }
    
    /**
     * 演示Jackson中的注解处理算法
     */
    public void demonstrateAnnotationProcessing() {
        System.out.println("\n5. Jackson注解处理算法");
        
        AnnotationProcessor processor = new AnnotationProcessor();
        
        // 模拟带有Jackson注解的对象
        AnnotatedPerson annotatedPerson = new AnnotatedPerson();
        annotatedPerson.setFullName("John Smith");
        annotatedPerson.setPersonAge(35);
        annotatedPerson.setEmailAddress("john@example.com");
        annotatedPerson.setInternalId(12345);
        
        System.out.println("   带注解对象: " + annotatedPerson);
        
        String processedJson = processor.processAnnotationsAndSerialize(annotatedPerson);
        System.out.println("   注解处理后的JSON: " + processedJson);
        
        // 显示注解处理规则
        System.out.println("   注解处理规则:");
        System.out.println("   - @JsonProperty: 重命名字段");
        System.out.println("   - @JsonIgnore: 忽略字段");
        System.out.println("   - @JsonInclude: 控制空值包含");
    }
    
    /**
     * 演示Jackson中的流式处理算法
     */
    public void demonstrateStreamingAlgorithm() {
        System.out.println("\n6. Jackson流式处理算法");
        
        StreamingProcessor streamingProcessor = new StreamingProcessor();
        
        // 模拟大型JSON数组的流式处理
        String largeJsonArray = "[\n" +
                               "  {\"id\":1, \"name\":\"Item1\", \"value\":100},\n" +
                               "  {\"id\":2, \"name\":\"Item2\", \"value\":200},\n" +
                               "  {\"id\":3, \"name\":\"Item3\", \"value\":300}\n" +
                               "]";
        
        System.out.println("   大型JSON数组流式处理:");
        streamingProcessor.processJsonStream(largeJsonArray, item -> {
            System.out.println("     处理项: " + item);
        });
        
        // 演示过滤算法
        System.out.println("   应用过滤器，只处理value > 150的项:");
        streamingProcessor.processJsonStreamWithFilter(largeJsonArray, 
            item -> {
                if (item instanceof Map) {
                    Object valueObj = ((Map)item).get("value");
                    if (valueObj instanceof Number) {
                        return ((Number)valueObj).intValue() > 150;
                    }
                }
                return false;
            },
            item -> System.out.println("     过滤后的项: " + item));
    }
    
    // 内部类实现
    static class JsonParser {
        private int position = 0;
        private String input;
        
        public Object parse(String json) {
            this.input = json.replaceAll("\\s+", ""); // 移除空白字符
            this.position = 0;
            return parseValue();
        }
        
        private Object parseValue() {
            if (position >= input.length()) {
                throw new RuntimeException("Unexpected end of input");
            }
            
            char currentChar = input.charAt(position);
            
            if (currentChar == '{') {
                return parseObject();
            } else if (currentChar == '[') {
                return parseArray();
            } else if (currentChar == '"') {
                return parseString();
            } else if (Character.isDigit(currentChar) || currentChar == '-') {
                return parseNumber();
            } else if (position + 4 <= input.length() && input.startsWith("true", position)) {
                position += 4;
                return true;
            } else if (position + 5 <= input.length() && input.startsWith("false", position)) {
                position += 5;
                return false;
            } else if (position + 4 <= input.length() && input.startsWith("null", position)) {
                position += 4;
                return null;
            }
            
            throw new RuntimeException("Unexpected character: " + currentChar);
        }
        
        private Map<String, Object> parseObject() {
            Map<String, Object> obj = new LinkedHashMap<>();
            position++; // 跳过 '{'
            
            if (position >= input.length() || input.charAt(position) == '}') {
                if (position < input.length()) {
                    position++;
                }
                return obj;
            }
            
            while (position < input.length()) {
                String key = (String) parseString();
                
                if (position >= input.length() || input.charAt(position) != ':') {
                    break;
                }
                position++; // 跳过 ':'
                
                Object value = parseValue();
                obj.put(key, value);
                
                if (position >= input.length()) {
                    break;
                }
                
                if (input.charAt(position) == '}') {
                    position++;
                    break;
                }
                
                if (input.charAt(position) == ',') {
                    position++; // 跳过 ','
                }
            }
            
            return obj;
        }
        
        private List<Object> parseArray() {
            List<Object> arr = new ArrayList<>();
            position++; // 跳过 '['
            
            if (position >= input.length() || input.charAt(position) == ']') {
                if (position < input.length()) {
                    position++;
                }
                return arr;
            }
            
            while (position < input.length()) {
                arr.add(parseValue());
                
                if (position >= input.length()) {
                    break;
                }
                
                if (input.charAt(position) == ']') {
                    position++;
                    break;
                }
                
                if (input.charAt(position) == ',') {
                    position++; // 跳过 ','
                }
            }
            
            return arr;
        }
        
        private String parseString() {
            position++; // 跳过 '"'
            int start = position;
            
            while (position < input.length() && input.charAt(position) != '"') {
                if (position + 1 < input.length() && input.charAt(position) == '\\') {
                    position += 2; // 跳过转义字符
                } else {
                    position++;
                }
            }
            
            if (position >= input.length()) {
                throw new RuntimeException("Unterminated string at position " + start);
            }
            
            String result = input.substring(start, position);
            position++; // 跳过 '"'
            return result;
        }
        
        private Number parseNumber() {
            int start = position;
            
            if (input.charAt(position) == '-') {
                position++;
            }
            
            while (position < input.length() && 
                   (Character.isDigit(input.charAt(position)) || input.charAt(position) == '.')) {
                position++;
            }
            
            String numStr = input.substring(start, position);
            return numStr.contains(".") ? Double.parseDouble(numStr) : Long.parseLong(numStr);
        }
    }
    
    static class SerializationEngine {
        public String serialize(Object obj) {
            // 简化的序列化算法
            StringBuilder sb = new StringBuilder();
            serializeObject(obj, sb);
            return sb.toString();
        }
        
        private void serializeObject(Object obj, StringBuilder sb) {
            if (obj == null) {
                sb.append("null");
                return;
            }
            
            Class<?> clazz = obj.getClass();
            if (clazz == String.class) {
                sb.append("\"").append(obj).append("\"");
            } else if (clazz == Integer.class || clazz == Long.class || 
                      clazz == Float.class || clazz == Double.class ||
                      clazz == Boolean.class) {
                sb.append(obj.toString());
            } else if (obj instanceof List) {
                serializeList((List<?>) obj, sb);
            } else if (obj instanceof Map) {
                serializeMap((Map<?, ?>) obj, sb);
            } else {
                // 使用反射序列化自定义对象
                serializeCustomObject(obj, sb);
            }
        }
        
        private void serializeList(List<?> list, StringBuilder sb) {
            sb.append("[");
            for (int i = 0; i < list.size(); i++) {
                if (i > 0) sb.append(",");
                serializeObject(list.get(i), sb);
            }
            sb.append("]");
        }
        
        private void serializeMap(Map<?, ?> map, StringBuilder sb) {
            sb.append("{");
            boolean first = true;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (!first) sb.append(",");
                first = false;
                sb.append("\"").append(entry.getKey()).append("\":");
                serializeObject(entry.getValue(), sb);
            }
            sb.append("}");
        }
        
        private void serializeCustomObject(Object obj, StringBuilder sb) {
            sb.append("{");
            // 简化处理，只处理基本字段
            if (obj instanceof Person) {
                Person person = (Person) obj;
                List<String> parts = new ArrayList<>();
                parts.add("\"name\":\"" + person.getName() + "\"");
                parts.add("\"age\":" + person.getAge());
                parts.add("\"email\":\"" + person.getEmail() + "\"");
                
                StringBuilder skillsSb = new StringBuilder();
                skillsSb.append("[");
                if (person.getSkills() != null) {
                    for (int i = 0; i < person.getSkills().size(); i++) {
                        if (i > 0) skillsSb.append(",");
                        skillsSb.append("\"").append(person.getSkills().get(i)).append("\"");
                    }
                }
                skillsSb.append("]");
                parts.add("\"skills\":" + skillsSb.toString());
                
                for (int i = 0; i < parts.size(); i++) {
                    if (i > 0) sb.append(",");
                    sb.append(parts.get(i));
                }
            }
            sb.append("}");
        }
        
        public <T> T deserialize(String json, Class<T> clazz) {
            // 简化的反序列化
            JsonParser parser = new JsonParser();
            Object parsed = parser.parse(json);
            
            if (clazz == Person.class) {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) parsed;
                Person person = new Person();
                person.setName((String) map.get("name"));
                person.setAge(((Number) map.get("age")).intValue());
                person.setEmail((String) map.get("email"));
                
                @SuppressWarnings("unchecked")
                List<String> skills = (List<String>) map.get("skills");
                person.setSkills(skills);
                
                return clazz.cast(person);
            }
            
            return null;
        }
    }
    
    static class TreeModel {
        public TreeNode createObjectNode() {
            return new TreeNode(true); // isObject = true
        }
        
        public TreeNode createArrayNode() {
            return new TreeNode(false); // isObject = false
        }
        
        public String toJson(TreeNode node) {
            return node.toJson();
        }
        
        public void traverse(TreeNode node, int depth) {
            String indent = "  ".repeat(depth);
            if (node.isObject()) {
                System.out.println(indent + "OBJECT {");
                for (Map.Entry<String, TreeNode> entry : node.getProperties().entrySet()) {
                    System.out.println(indent + "  " + entry.getKey() + ":");
                    traverse(entry.getValue(), depth + 2);
                }
                System.out.println(indent + "}");
            } else if (node.isArray()) {
                System.out.println(indent + "ARRAY [");
                for (TreeNode item : node.getItems()) {
                    traverse(item, depth + 1);
                }
                System.out.println(indent + "]");
            } else {
                System.out.println(indent + "VALUE: " + node.getValue());
            }
        }
    }
    
    static class TreeNode {
        private final boolean isObject;
        private final boolean isArray;
        private Object value;
        private Map<String, TreeNode> properties;
        private List<TreeNode> items;
        
        public TreeNode(boolean isObject) {
            this.isObject = isObject;
            this.isArray = !isObject;
            
            if (isObject) {
                this.properties = new LinkedHashMap<>();
            } else if (isArray) {
                this.items = new ArrayList<>();
            }
        }
        
        public TreeNode(Object value) {
            this.isObject = false;
            this.isArray = false;
            this.value = value;
        }
        
        public void put(String key, Object value) {
            if (isObject) {
                properties.put(key, new TreeNode(value));
            }
        }
        
        public void set(String key, TreeNode node) {
            if (isObject) {
                properties.put(key, node);
            }
        }
        
        public void add(Object value) {
            if (isArray) {
                items.add(new TreeNode(value));
            }
        }
        
        public void set(TreeNode node) {
            if (isArray) {
                items.add(node);
            }
        }
        
        public boolean isObject() { return isObject; }
        public boolean isArray() { return isArray; }
        public Object getValue() { return value; }
        public Map<String, TreeNode> getProperties() { return properties; }
        public List<TreeNode> getItems() { return items; }
        
        public String toJson() {
            StringBuilder sb = new StringBuilder();
            toJson(this, sb);
            return sb.toString();
        }
        
        private void toJson(TreeNode node, StringBuilder sb) {
            if (node.isObject) {
                sb.append("{");
                boolean first = true;
                for (Map.Entry<String, TreeNode> entry : node.properties.entrySet()) {
                    if (!first) sb.append(",");
                    first = false;
                    sb.append("\"").append(entry.getKey()).append("\":");
                    toJson(entry.getValue(), sb);
                }
                sb.append("}");
            } else if (node.isArray) {
                sb.append("[");
                for (int i = 0; i < node.items.size(); i++) {
                    if (i > 0) sb.append(",");
                    toJson(node.items.get(i), sb);
                }
                sb.append("]");
            } else {
                if (node.value instanceof String) {
                    sb.append("\"").append(node.value).append("\"");
                } else if (node.value instanceof Number) {
                    sb.append(node.value);
                } else if (node.value instanceof Boolean) {
                    sb.append(node.value);
                } else {
                    sb.append("null");
                }
            }
        }
    }
    
    static class TypeResolver {
        private final Map<String, Class<?>> typeMap = new HashMap<>();
        
        public void registerType(String type, Class<?> clazz) {
            typeMap.put(type, clazz);
        }
        
        public Object resolveAndDeserialize(String json) {
            JsonParser parser = new JsonParser();
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) parser.parse(json);
            
            String type = (String) map.get("type");
            Class<?> clazz = typeMap.get(type);
            
            if (clazz == Person.class) {
                Person person = new Person();
                person.setName((String) map.get("name"));
                person.setAge(((Number) map.get("age")).intValue());
                return person;
            } else if (clazz == Employee.class) {
                Employee emp = new Employee();
                emp.setName((String) map.get("name"));
                emp.setAge(((Number) map.get("age")).intValue());
                emp.setDepartment((String) map.get("department"));
                return emp;
            } else if (clazz == Customer.class) {
                Customer cust = new Customer();
                cust.setName((String) map.get("name"));
                cust.setAge(((Number) map.get("age")).intValue());
                cust.setCustomerId((String) map.get("customerId"));
                return cust;
            }
            
            return null;
        }
    }
    
    static class AnnotationProcessor {
        public String processAnnotationsAndSerialize(Object obj) {
            if (obj instanceof AnnotatedPerson) {
                AnnotatedPerson person = (AnnotatedPerson) obj;
                Map<String, Object> result = new LinkedHashMap<>();
                
                // 应用 @JsonProperty 注解逻辑
                result.put("full_name", person.getFullName());
                result.put("person_age", person.getPersonAge());
                result.put("email_address", person.getEmailAddress());
                // @JsonIgnore 注解跳过 internalId
                
                return new SerializationEngine().serialize(result);
            }
            
            return "";
        }
    }
    
    static class StreamingProcessor {
        public void processJsonStream(String jsonArray, ItemProcessor processor) {
            // 简化的流式处理算法
            JsonParser parser = new JsonParser();
            @SuppressWarnings("unchecked")
            List<Object> array = (List<Object>) parser.parse(jsonArray);
            
            for (Object item : array) {
                processor.process(item);
            }
        }
        
        public void processJsonStreamWithFilter(String jsonArray, 
                                               ItemFilter filter, 
                                               ItemProcessor processor) {
            JsonParser parser = new JsonParser();
            @SuppressWarnings("unchecked")
            List<Object> array = (List<Object>) parser.parse(jsonArray);
            
            for (Object item : array) {
                if (filter.test(item)) {
                    processor.process(item);
                }
            }
        }
        
        interface ItemProcessor {
            void process(Object item);
        }
        
        interface ItemFilter {
            boolean test(Object item);
        }
    }
    
    // 示例类
    static class Person {
        private String name;
        private int age;
        private String email;
        private List<String> skills;
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public List<String> getSkills() { return skills; }
        public void setSkills(List<String> skills) { this.skills = skills; }
        
        @Override
        public String toString() {
            return "Person{name='" + name + "', age=" + age + ", email='" + email + "', skills=" + skills + "}";
        }
    }
    
    static class Employee extends Person {
        private String department;
        
        public String getDepartment() { return department; }
        public void setDepartment(String department) { this.department = department; }
        
        @Override
        public String toString() {
            return "Employee{name='" + getName() + "', age=" + getAge() + ", department='" + department + "'}";
        }
    }
    
    static class Customer extends Person {
        private String customerId;
        
        public String getCustomerId() { return customerId; }
        public void setCustomerId(String customerId) { this.customerId = customerId; }
        
        @Override
        public String toString() {
            return "Customer{name='" + getName() + "', age=" + getAge() + ", customerId='" + customerId + "'}";
        }
    }
    
    // 模拟Jackson注解
    @interface JsonProperty {
        String value();
    }
    
    @interface JsonIgnore {}
    
    @interface JsonInclude {}
    
    static class AnnotatedPerson {
        @JsonProperty("full_name")
        private String fullName;
        
        @JsonProperty("person_age")
        private int personAge;
        
        @JsonProperty("email_address")
        private String emailAddress;
        
        @JsonIgnore
        private int internalId;
        
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public int getPersonAge() { return personAge; }
        public void setPersonAge(int personAge) { this.personAge = personAge; }
        public String getEmailAddress() { return emailAddress; }
        public void setEmailAddress(String emailAddress) { this.emailAddress = emailAddress; }
        public int getInternalId() { return internalId; }
        public void setInternalId(int internalId) { this.internalId = internalId; }
        
        @Override
        public String toString() {
            return "AnnotatedPerson{fullName='" + fullName + "', personAge=" + personAge + 
                   ", emailAddress='" + emailAddress + "', internalId=" + internalId + "}";
        }
    }
    
    public void demonstrate() {
        demonstrateJsonParsingAlgorithm();
        demonstrateSerializationAlgorithm();
        demonstrateTreeModelAlgorithm();
        demonstrateTypeResolutionAlgorithm();
        demonstrateAnnotationProcessing();
        demonstrateStreamingAlgorithm();
    }
}