package com.architecture.jdk;

import java.util.*;
import java.util.stream.*;
import java.util.function.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.nio.file.*;
import java.io.*;

/**
 * JDK核心特性和API详解
 * 
 * 重点内容：
 * 1. Stream API和函数式编程 (JDK 8+)
 * 2. 时间日期API (JDK 8+)
 * 3. Optional类使用
 * 4. Lambda表达式和方法引用
 * 5. 集合框架增强
 * 6. NIO.2文件操作
 * 7. 模块系统 (JDK 9+)
 * 8. 新的语言特性 (JDK 11+)
 */

public class JDKCoreFeatures {
    
    /**
     * JDK 8+ Stream API详解
     * 
     * 核心概念：
     * 1. 流的创建：Collection.stream()、Arrays.stream()、Stream.of()
     * 2. 中间操作：filter、map、sorted、distinct等
     * 3. 终端操作：collect、forEach、reduce、count等
     * 4. 并行流：parallelStream()
     */
    public static void demonstrateStreamAPI() {
        System.out.println("=== Stream API Demo ===");
        
        List<String> names = Arrays.asList(
            "Alice", "Bob", "Charlie", "David", "Eve", "Frank", "Grace", "Henry"
        );
        
        // 1. 基本流操作
        System.out.println("\n--- Basic Stream Operations ---");
        List<String> filteredNames = names.stream()
            .filter(name -> name.length() > 4)
            .map(String::toUpperCase)
            .sorted()
            .collect(Collectors.toList());
        
        System.out.println("Filtered and transformed names: " + filteredNames);
        
        // 2. 数值流操作
        System.out.println("\n--- Numeric Stream Operations ---");
        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        
        int sum = numbers.stream()
            .filter(n -> n % 2 == 0)
            .mapToInt(Integer::intValue)
            .sum();
        
        OptionalDouble average = numbers.stream()
            .mapToInt(Integer::intValue)
            .average();
        
        System.out.println("Sum of even numbers: " + sum);
        System.out.println("Average: " + average.orElse(0.0));
        
        // 3. 分组和分区
        System.out.println("\n--- Grouping and Partitioning ---");
        Map<Integer, List<String>> groupedByLength = names.stream()
            .collect(Collectors.groupingBy(String::length));
        
        System.out.println("Grouped by length: " + groupedByLength);
        
        Map<Boolean, List<String>> partitioned = names.stream()
            .collect(Collectors.partitioningBy(name -> name.length() > 4));
        
        System.out.println("Partitioned by length > 4: " + partitioned);
        
        // 4. 复杂的收集操作
        System.out.println("\n--- Complex Collectors ---");
        String joinedNames = names.stream()
            .filter(name -> name.startsWith("A") || name.startsWith("B"))
            .collect(Collectors.joining(", ", "[", "]"));
        
        System.out.println("Joined names starting with A or B: " + joinedNames);
        
        // 5. 并行流
        System.out.println("\n--- Parallel Stream ---");
        long parallelSum = IntStream.rangeClosed(1, 1000000)
            .parallel()
            .filter(n -> n % 2 == 0)
            .count();
        
        System.out.println("Count of even numbers (1-1000000): " + parallelSum);
        
        // 6. 自定义收集器
        System.out.println("\n--- Custom Collector ---");
        String customCollected = names.stream()
            .collect(Collector.of(
                StringBuilder::new,                    // supplier
                (sb, s) -> sb.append(s).append("|"),  // accumulator
                StringBuilder::append,                 // combiner
                StringBuilder::toString               // finisher
            ));
        
        System.out.println("Custom collected: " + customCollected);
    }
    
    /**
     * JDK 8+ 时间日期API详解
     * 
     * 核心类：
     * 1. LocalDate、LocalTime、LocalDateTime
     * 2. ZonedDateTime、OffsetDateTime
     * 3. Duration、Period
     * 4. DateTimeFormatter
     */
    public static void demonstrateDateTimeAPI() {
        System.out.println("\n=== Date Time API Demo ===");
        
        // 1. 基本日期时间操作
        System.out.println("\n--- Basic Date Time Operations ---");
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        LocalDateTime dateTime = LocalDateTime.now();
        
        System.out.println("Today: " + today);
        System.out.println("Now: " + now);
        System.out.println("DateTime: " + dateTime);
        
        // 2. 日期计算
        System.out.println("\n--- Date Calculations ---");
        LocalDate nextWeek = today.plusWeeks(1);
        LocalDate lastMonth = today.minusMonths(1);
        LocalDate specificDate = LocalDate.of(2024, Month.DECEMBER, 25);
        
        System.out.println("Next week: " + nextWeek);
        System.out.println("Last month: " + lastMonth);
        System.out.println("Christmas 2024: " + specificDate);
        
        // 3. 时区处理
        System.out.println("\n--- Time Zone Handling ---");
        ZonedDateTime utcTime = ZonedDateTime.now(ZoneId.of("UTC"));
        ZonedDateTime tokyoTime = ZonedDateTime.now(ZoneId.of("Asia/Tokyo"));
        ZonedDateTime newYorkTime = ZonedDateTime.now(ZoneId.of("America/New_York"));
        
        System.out.println("UTC: " + utcTime);
        System.out.println("Tokyo: " + tokyoTime);
        System.out.println("New York: " + newYorkTime);
        
        // 4. 时间间隔计算
        System.out.println("\n--- Duration and Period ---");
        LocalDateTime start = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime end = LocalDateTime.of(2024, 1, 1, 15, 30);
        
        Duration duration = Duration.between(start, end);
        System.out.println("Duration: " + duration.toHours() + " hours " + 
            (duration.toMinutes() % 60) + " minutes");
        
        LocalDate birthDate = LocalDate.of(1990, 5, 15);
        Period age = Period.between(birthDate, today);
        System.out.println("Age: " + age.getYears() + " years " + 
            age.getMonths() + " months " + age.getDays() + " days");
        
        // 5. 格式化和解析
        System.out.println("\n--- Formatting and Parsing ---");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formatted = dateTime.format(formatter);
        System.out.println("Formatted: " + formatted);
        
        LocalDateTime parsed = LocalDateTime.parse("2024-12-25 18:30:00", formatter);
        System.out.println("Parsed: " + parsed);
        
        // 6. 常用格式化器
        System.out.println("\n--- Common Formatters ---");
        System.out.println("ISO_LOCAL_DATE: " + today.format(DateTimeFormatter.ISO_LOCAL_DATE));
        System.out.println("ISO_LOCAL_TIME: " + now.format(DateTimeFormatter.ISO_LOCAL_TIME));
        System.out.println("ISO_LOCAL_DATE_TIME: " + dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    }
    
    /**
     * Optional类详解
     * 
     * 核心概念：
     * 1. 避免NullPointerException
     * 2. 函数式编程风格
     * 3. 链式调用
     */
    public static void demonstrateOptional() {
        System.out.println("\n=== Optional Demo ===");
        
        // 1. Optional创建
        System.out.println("\n--- Optional Creation ---");
        Optional<String> empty = Optional.empty();
        Optional<String> nonEmpty = Optional.of("Hello World");
        Optional<String> nullable = Optional.ofNullable(null);
        
        System.out.println("Empty: " + empty);
        System.out.println("Non-empty: " + nonEmpty);
        System.out.println("Nullable: " + nullable);
        
        // 2. Optional检查和获取
        System.out.println("\n--- Optional Checking and Getting ---");
        if (nonEmpty.isPresent()) {
            System.out.println("Value present: " + nonEmpty.get());
        }
        
        nonEmpty.ifPresent(value -> System.out.println("Using ifPresent: " + value));
        
        String defaultValue = empty.orElse("Default Value");
        System.out.println("With default: " + defaultValue);
        
        String lazyDefault = empty.orElseGet(() -> "Lazy Default: " + System.currentTimeMillis());
        System.out.println("With lazy default: " + lazyDefault);
        
        // 3. Optional转换
        System.out.println("\n--- Optional Transformation ---");
        Optional<String> upperCase = nonEmpty.map(String::toUpperCase);
        System.out.println("Mapped to uppercase: " + upperCase);
        
        Optional<Integer> length = nonEmpty.map(String::length);
        System.out.println("Mapped to length: " + length);
        
        // 4. Optional过滤
        System.out.println("\n--- Optional Filtering ---");
        Optional<String> filtered = nonEmpty.filter(s -> s.length() > 5);
        System.out.println("Filtered (length > 5): " + filtered);
        
        Optional<String> filteredOut = nonEmpty.filter(s -> s.length() > 20);
        System.out.println("Filtered out (length > 20): " + filteredOut);
        
        // 5. Optional链式调用
        System.out.println("\n--- Optional Chaining ---");
        String result = Optional.of("  Hello World  ")
            .filter(s -> !s.trim().isEmpty())
            .map(String::trim)
            .map(String::toUpperCase)
            .orElse("EMPTY");
        
        System.out.println("Chained result: " + result);
        
        // 6. 实际应用示例
        System.out.println("\n--- Practical Example ---");
        List<Person> people = Arrays.asList(
            new Person("Alice", 25),
            new Person("Bob", 30),
            new Person("Charlie", 35)
        );
        
        Optional<Person> found = findPersonByName(people, "Bob");
        found.ifPresentOrElse(
            person -> System.out.println("Found: " + person),
            () -> System.out.println("Person not found")
        );
        
        Optional<Person> notFound = findPersonByName(people, "David");
        notFound.ifPresentOrElse(
            person -> System.out.println("Found: " + person),
            () -> System.out.println("Person not found")
        );
    }
    
    private static Optional<Person> findPersonByName(List<Person> people, String name) {
        return people.stream()
            .filter(person -> person.getName().equals(name))
            .findFirst();
    }
    
    static class Person {
        private String name;
        private int age;
        
        public Person(String name, int age) {
            this.name = name;
            this.age = age;
        }
        
        public String getName() { return name; }
        public int getAge() { return age; }
        
        @Override
        public String toString() {
            return "Person{name='" + name + "', age=" + age + "}";
        }
    }
    
    /**
     * Lambda表达式和方法引用详解
     * 
     * 核心概念：
     * 1. 函数式接口
     * 2. Lambda语法
     * 3. 方法引用类型
     * 4. 闭包和变量捕获
     */
    public static void demonstrateLambdaAndMethodReference() {
        System.out.println("\n=== Lambda and Method Reference Demo ===");
        
        List<String> words = Arrays.asList("apple", "banana", "cherry", "date");
        
        // 1. Lambda表达式基本语法
        System.out.println("\n--- Lambda Syntax ---");
        
        // 传统匿名内部类
        words.sort(new Comparator<String>() {
            @Override
            public int compare(String a, String b) {
                return a.length() - b.length();
            }
        });
        System.out.println("Sorted by length (anonymous class): " + words);
        
        // Lambda表达式
        words.sort((a, b) -> b.length() - a.length());
        System.out.println("Sorted by length desc (lambda): " + words);
        
        // 2. 方法引用类型
        System.out.println("\n--- Method Reference Types ---");
        
        List<String> names = Arrays.asList("alice", "bob", "charlie");
        
        // 静态方法引用
        names.stream()
            .map(String::toUpperCase)  // 等价于 s -> String.toUpperCase(s)
            .forEach(System.out::println);  // 等价于 s -> System.out.println(s)
        
        // 实例方法引用
        String prefix = "Name: ";
        names.stream()
            .map(prefix::concat)  // 等价于 s -> prefix.concat(s)
            .forEach(System.out::println);
        
        // 构造器引用
        List<Integer> lengths = names.stream()
            .map(String::length)
            .collect(Collectors.toList());
        System.out.println("Lengths: " + lengths);
        
        // 3. 常用函数式接口
        System.out.println("\n--- Functional Interfaces ---");
        
        // Predicate<T> - 断言
        Predicate<String> isLong = s -> s.length() > 5;
        System.out.println("Long words: " + 
            names.stream().filter(isLong).collect(Collectors.toList()));
        
        // Function<T, R> - 函数
        Function<String, Integer> getLength = String::length;
        System.out.println("Lengths: " + 
            names.stream().map(getLength).collect(Collectors.toList()));
        
        // Consumer<T> - 消费者
        Consumer<String> printer = s -> System.out.println("Processing: " + s);
        names.forEach(printer);
        
        // Supplier<T> - 供应者
        Supplier<String> randomName = () -> names.get((int)(Math.random() * names.size()));
        System.out.println("Random name: " + randomName.get());
        
        // 4. 复合函数
        System.out.println("\n--- Function Composition ---");
        
        Function<String, String> addPrefix = s -> "Mr. " + s;
        Function<String, String> addSuffix = s -> s + " Jr.";
        Function<String, String> combined = addPrefix.andThen(addSuffix);
        
        System.out.println("Combined function: " + combined.apply("John"));
        
        // 5. 闭包和变量捕获
        System.out.println("\n--- Closures and Variable Capture ---");
        
        final String finalVar = "Final";
        int effectivelyFinal = 42;
        
        Runnable closure = () -> {
            System.out.println("Captured final variable: " + finalVar);
            System.out.println("Captured effectively final variable: " + effectivelyFinal);
        };
        
        closure.run();
    }
    
    /**
     * 集合框架增强特性
     * 
     * JDK 8+新增：
     * 1. 默认方法
     * 2. Stream支持
     * 3. 新的工厂方法 (JDK 9+)
     */
    public static void demonstrateCollectionEnhancements() {
        System.out.println("\n=== Collection Enhancements Demo ===");
        
        // 1. List增强
        System.out.println("\n--- List Enhancements ---");
        List<String> list = new ArrayList<>(Arrays.asList("a", "b", "c", "d", "e"));
        
        // removeIf - JDK 8
        list.removeIf(s -> s.equals("c"));
        System.out.println("After removeIf: " + list);
        
        // replaceAll - JDK 8
        list.replaceAll(String::toUpperCase);
        System.out.println("After replaceAll: " + list);
        
        // sort - JDK 8
        list.sort(Comparator.reverseOrder());
        System.out.println("After sort: " + list);
        
        // 2. Map增强
        System.out.println("\n--- Map Enhancements ---");
        Map<String, Integer> map = new HashMap<>();
        map.put("apple", 5);
        map.put("banana", 3);
        map.put("cherry", 8);
        
        // forEach - JDK 8
        System.out.println("Map contents:");
        map.forEach((k, v) -> System.out.println(k + " -> " + v));
        
        // computeIfAbsent - JDK 8
        map.computeIfAbsent("date", k -> k.length());
        System.out.println("After computeIfAbsent: " + map);
        
        // merge - JDK 8
        map.merge("apple", 2, Integer::sum);
        System.out.println("After merge: " + map);
        
        // 3. 不可变集合工厂方法 (JDK 9+)
        System.out.println("\n--- Immutable Collections (JDK 9+) ---");
        try {
            List<String> immutableList = List.of("x", "y", "z");
            Set<String> immutableSet = Set.of("a", "b", "c");
            Map<String, Integer> immutableMap = Map.of("one", 1, "two", 2, "three", 3);
            
            System.out.println("Immutable list: " + immutableList);
            System.out.println("Immutable set: " + immutableSet);
            System.out.println("Immutable map: " + immutableMap);
        } catch (Exception e) {
            System.out.println("Immutable collections not available (requires JDK 9+)");
        }
    }
    
    public static void main(String[] args) {
        demonstrateStreamAPI();
        demonstrateDateTimeAPI();
        demonstrateOptional();
        demonstrateLambdaAndMethodReference();
        demonstrateCollectionEnhancements();
    }
}