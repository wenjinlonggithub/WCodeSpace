# 分层架构 (Layered Architecture)

## 一、架构概述

分层架构是最常见的软件架构模式之一,将应用程序划分为若干水平的层次,每一层都有明确的职责,上层依赖下层,下层对上层提供服务。这是一种经典的关注点分离(Separation of Concerns)的实现方式。

## 二、解决的业务问题

### 1. 混乱代码的痛点
- **职责混乱**: UI、业务逻辑、数据访问混在一起
- **难以维护**: 修改一个功能影响多处代码
- **无法复用**: 业务逻辑与UI紧耦合
- **测试困难**: 无法独立测试各个部分

### 2. 分层架构的解决方案
- **职责分离**: 每层负责特定的职责
- **易于维护**: 修改只影响特定层
- **可复用**: 下层可以被多个上层复用
- **易于测试**: 可以独立测试每一层

### 3. 适用场景
- **传统企业应用**: ERP、CRM等
- **Web应用**: 电商、内容管理系统
- **中小型项目**: 业务逻辑中等复杂度
- **快速开发**: 团队熟悉的标准模式

## 三、核心架构

### 1. 经典四层架构

```
┌────────────────────────────────────┐
│      表现层 (Presentation)         │  ← Controller, View, DTO
├────────────────────────────────────┤
│      业务逻辑层 (Business Logic)    │  ← Service, 业务规则
├────────────────────────────────────┤
│      持久层 (Persistence)           │  ← DAO, Repository
├────────────────────────────────────┤
│      数据库层 (Database)            │  ← MySQL, Oracle
└────────────────────────────────────┘

依赖关系: 上层 → 下层
```

### 2. 各层职责

#### 表现层 (Presentation Layer)
```java
// Controller - 处理HTTP请求
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    // 处理用户注册请求
    @PostMapping("/register")
    public ResponseEntity<UserDTO> register(@RequestBody RegisterRequest request) {
        // 1. 参数校验
        validateRequest(request);

        // 2. 调用业务层
        User user = userService.register(
            request.getUsername(),
            request.getPassword(),
            request.getEmail()
        );

        // 3. 转换为DTO返回
        UserDTO dto = convertToDTO(user);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUser(@PathVariable Long id) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(convertToDTO(user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> updateUser(
        @PathVariable Long id,
        @RequestBody UpdateUserRequest request
    ) {
        User user = userService.updateUser(id, request);
        return ResponseEntity.ok(convertToDTO(user));
    }

    private void validateRequest(RegisterRequest request) {
        if (request.getUsername() == null || request.getUsername().length() < 3) {
            throw new ValidationException("用户名至少3个字符");
        }
        if (request.getPassword() == null || request.getPassword().length() < 6) {
            throw new ValidationException("密码至少6个字符");
        }
    }

    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }
}

// DTO - 数据传输对象
public class UserDTO {
    private Long id;
    private String username;
    private String email;
    private LocalDateTime createdAt;
    // Getters, Setters
}

public class RegisterRequest {
    private String username;
    private String password;
    private String email;
    // Getters, Setters
}
```

#### 业务逻辑层 (Business Logic Layer)
```java
// Service - 业务逻辑
@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    /**
     * 用户注册
     */
    public User register(String username, String password, String email) {
        // 1. 业务验证
        validateUsername(username);
        validateEmail(email);

        // 2. 检查用户是否已存在
        if (userRepository.existsByUsername(username)) {
            throw new UserAlreadyExistsException("用户名已存在");
        }
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException("邮箱已被使用");
        }

        // 3. 创建用户
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setEmail(email);
        user.setStatus(UserStatus.ACTIVE);
        user.setCreatedAt(LocalDateTime.now());

        // 4. 保存用户
        User savedUser = userRepository.save(user);

        // 5. 发送欢迎邮件
        emailService.sendWelcomeEmail(email, username);

        return savedUser;
    }

    /**
     * 获取用户
     */
    public User getUserById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException("用户不存在"));
    }

    /**
     * 更新用户信息
     */
    public User updateUser(Long id, UpdateUserRequest request) {
        User user = getUserById(id);

        if (request.getEmail() != null) {
            validateEmail(request.getEmail());
            user.setEmail(request.getEmail());
        }

        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }

        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    /**
     * 用户登录
     */
    public User login(String username, String password) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new InvalidCredentialsException("用户名或密码错误"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new InvalidCredentialsException("用户名或密码错误");
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new UserInactiveException("用户已被禁用");
        }

        // 更新最后登录时间
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        return user;
    }

    private void validateUsername(String username) {
        if (username.length() < 3 || username.length() > 20) {
            throw new ValidationException("用户名长度必须在3-20之间");
        }
        if (!username.matches("^[a-zA-Z0-9_]+$")) {
            throw new ValidationException("用户名只能包含字母、数字和下划线");
        }
    }

    private void validateEmail(String email) {
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new ValidationException("邮箱格式不正确");
        }
    }
}

// 领域模型
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(unique = true, nullable = false)
    private String email;

    private String phone;

    @Enumerated(EnumType.STRING)
    private UserStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLoginAt;

    // Getters, Setters
}

public enum UserStatus {
    ACTIVE, INACTIVE, BANNED
}
```

#### 持久层 (Persistence Layer)
```java
// Repository接口 - Spring Data JPA
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.status = :status")
    List<User> findByStatus(@Param("status") UserStatus status);

    @Query("SELECT u FROM User u WHERE u.createdAt >= :startDate")
    List<User> findRecentUsers(@Param("startDate") LocalDateTime startDate);
}

// 自定义Repository实现
@Repository
public class UserRepositoryImpl {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<User> findUsersByComplexCriteria(UserSearchCriteria criteria) {
        StringBuilder sql = new StringBuilder("SELECT * FROM users WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (criteria.getUsername() != null) {
            sql.append(" AND username LIKE ?");
            params.add("%" + criteria.getUsername() + "%");
        }

        if (criteria.getEmail() != null) {
            sql.append(" AND email LIKE ?");
            params.add("%" + criteria.getEmail() + "%");
        }

        if (criteria.getStatus() != null) {
            sql.append(" AND status = ?");
            params.add(criteria.getStatus().name());
        }

        return jdbcTemplate.query(
            sql.toString(),
            params.toArray(),
            new UserRowMapper()
        );
    }
}

// DAO模式(传统方式)
@Repository
public class UserDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public User findById(Long id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, new UserRowMapper(), id);
    }

    public void save(User user) {
        String sql = "INSERT INTO users (username, password, email, status, created_at) " +
                    "VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql,
            user.getUsername(),
            user.getPassword(),
            user.getEmail(),
            user.getStatus().name(),
            user.getCreatedAt()
        );
    }

    public void update(User user) {
        String sql = "UPDATE users SET email = ?, phone = ?, updated_at = ? WHERE id = ?";
        jdbcTemplate.update(sql,
            user.getEmail(),
            user.getPhone(),
            user.getUpdatedAt(),
            user.getId()
        );
    }

    public void delete(Long id) {
        String sql = "DELETE FROM users WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }
}
```

### 3. 跨层对象

#### DTO vs Entity vs VO
```java
// Entity - 数据库实体(持久层)
@Entity
public class Product {
    @Id
    private Long id;
    private String name;
    private BigDecimal price;
    private String description;
    private Integer stock;
    private LocalDateTime createdAt;
    // 包含所有数据库字段
}

// DTO - 数据传输对象(表现层)
public class ProductDTO {
    private Long id;
    private String name;
    private BigDecimal price;
    private Integer stock;
    // 只包含需要传输的字段,可能聚合多个Entity
}

// VO - 值对象(业务层)
public class ProductDetailVO {
    private Long id;
    private String name;
    private String formattedPrice;  // 格式化后的价格
    private String stockStatus;  // 库存状态(充足/紧张/缺货)
    private boolean isOnSale;  // 是否在售
    // 包含业务逻辑处理后的数据
}

// 转换示例
@Service
public class ProductService {

    public ProductDetailVO getProductDetail(Long id) {
        // 1. 从持久层获取Entity
        Product product = productRepository.findById(id);

        // 2. 转换为VO
        ProductDetailVO vo = new ProductDetailVO();
        vo.setId(product.getId());
        vo.setName(product.getName());
        vo.setFormattedPrice("¥" + product.getPrice());

        // 业务逻辑
        if (product.getStock() > 100) {
            vo.setStockStatus("充足");
        } else if (product.getStock() > 0) {
            vo.setStockStatus("紧张");
        } else {
            vo.setStockStatus("缺货");
        }

        vo.setOnSale(product.getStock() > 0);

        return vo;
    }
}
```

## 四、分层变体

### 1. 严格分层 vs 松散分层

```java
// 严格分层:每层只能访问下一层
Controller → Service → Repository → Database

// 松散分层:可以跨层访问
Controller → Service → Repository
     │                    ↑
     └────────────────────┘  (允许Controller直接访问Repository)
```

### 2. 五层架构

```
┌────────────────────────┐
│      表现层             │  Controller, View
├────────────────────────┤
│      业务逻辑层          │  Service
├────────────────────────┤
│      领域模型层          │  Domain Model, Entity
├────────────────────────┤
│      持久层             │  Repository, DAO
├────────────────────────┤
│      数据库层           │  Database
└────────────────────────┘
```

### 3. MVC分层

```
┌──────────────┐
│    View      │  ← 视图层
└──────┬───────┘
       │
┌──────▼───────┐
│  Controller  │  ← 控制层
└──────┬───────┘
       │
┌──────▼───────┐
│    Model     │  ← 模型层(包含业务逻辑和数据访问)
└──────────────┘
```

```java
// Spring MVC示例
@Controller
public class UserController {

    @Autowired
    private UserService userService;

    // 显示用户列表页面
    @GetMapping("/users")
    public String listUsers(Model model) {
        List<User> users = userService.getAllUsers();
        model.addAttribute("users", users);
        return "user/list";  // 返回视图名称
    }

    // 显示用户详情页面
    @GetMapping("/users/{id}")
    public String userDetail(@PathVariable Long id, Model model) {
        User user = userService.getUserById(id);
        model.addAttribute("user", user);
        return "user/detail";
    }

    // 处理表单提交
    @PostMapping("/users")
    public String createUser(@ModelAttribute UserForm form) {
        userService.createUser(form);
        return "redirect:/users";
    }
}
```

## 五、最佳实践

### 1. 依赖注入

```java
// 推荐:构造函数注入
@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    // 构造函数注入(推荐)
    public OrderService(
        OrderRepository orderRepository,
        ProductRepository productRepository
    ) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }
}

// 不推荐:字段注入
@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;  // 难以测试

    @Autowired
    private ProductRepository productRepository;
}
```

### 2. 事务管理

```java
@Service
public class OrderService {

    // 方法级事务
    @Transactional
    public Order createOrder(CreateOrderRequest request) {
        // 1. 创建订单
        Order order = new Order();
        orderRepository.save(order);

        // 2. 扣减库存
        productService.reduceStock(request.getProductId(), request.getQuantity());

        // 3. 创建支付单
        paymentService.createPayment(order);

        // 三个操作在同一事务中,要么全部成功,要么全部失败
        return order;
    }

    // 只读事务
    @Transactional(readOnly = true)
    public List<Order> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserId(userId);
    }

    // 事务传播
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logOrderCreation(Order order) {
        // 新事务,不受外部事务影响
        auditLogRepository.save(new AuditLog(order));
    }
}
```

### 3. 异常处理

```java
// 统一异常处理
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException ex) {
        ErrorResponse error = new ErrorResponse(
            HttpStatus.NOT_FOUND.value(),
            ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidation(ValidationException ex) {
        ErrorResponse error = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            ex.getMessage()
        );
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
        log.error("未处理的异常", ex);
        ErrorResponse error = new ErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "系统错误"
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}

// 业务异常
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }
}
```

## 六、面试高频问题

### 1. 什么是分层架构?各层的职责是什么?

**答案要点**:
- **表现层**: 处理用户交互,HTTP请求响应
- **业务逻辑层**: 实现业务规则和流程
- **持久层**: 数据访问,CRUD操作
- **数据库层**: 数据存储

### 2. 为什么要分层?

**优势**:
- **关注点分离**: 每层专注自己的职责
- **易于维护**: 修改影响范围小
- **可复用**: 下层可被多个上层使用
- **易于测试**: 可以独立测试每层
- **团队协作**: 不同团队负责不同层

### 3. Service层和DAO层有什么区别?

**Service层(业务逻辑层)**:
- 处理业务逻辑
- 事务管理
- 协调多个DAO
- 业务验证

**DAO层(数据访问层)**:
- 数据库CRUD操作
- 无业务逻辑
- 专注数据访问

**示例**:
```java
// Service:包含业务逻辑
@Service
public class OrderService {
    public Order createOrder(CreateOrderRequest request) {
        // 业务验证
        validateOrder(request);

        // 协调多个DAO
        Order order = orderDAO.save(new Order(request));
        productDAO.updateStock(request.getProductId(), -request.getQuantity());
        paymentDAO.createPayment(order);

        return order;
    }
}

// DAO:只做数据访问
@Repository
public class OrderDAO {
    public Order save(Order order) {
        // 只负责保存数据
        return jdbcTemplate.save(order);
    }
}
```

### 4. DTO、Entity、VO有什么区别?

**Entity(实体)**:
- 对应数据库表
- 包含所有字段
- 用于持久层

**DTO(数据传输对象)**:
- 用于层间数据传输
- 只包含需要的字段
- 可能组合多个Entity

**VO(值对象)**:
- 业务视图对象
- 包含展示逻辑
- 不可变

### 5. 分层架构的缺点是什么?

**缺点**:
- **性能开销**: 层间调用和数据转换
- **过度工程**: 简单应用可能过于复杂
- **贫血模型**: 业务逻辑集中在Service
- **修改成本**: 跨多层的修改

### 6. 如何避免Service层过于臃肿?

**策略**:
1. **拆分Service**: 按业务领域拆分
2. **引入领域模型**: 业务逻辑放入Entity
3. **使用领域服务**: 提取跨实体逻辑
4. **应用CQRS**: 读写分离

### 7. 分层架构如何演进到微服务?

**演进路径**:
```
单体分层 → 模块化分层 → 垂直切分 → 微服务

1. 单体分层:传统三层架构
2. 模块化:按业务模块划分
3. 垂直切分:每个模块独立部署
4. 微服务:独立服务+独立数据库
```

### 8. 层与层之间如何通信?

**方式**:
- **直接调用**: 上层直接调用下层方法
- **接口调用**: 通过接口解耦
- **事件**: 层间事件通知
- **消息队列**: 异步通信

### 9. 如何测试分层架构?

**测试策略**:
```java
// 1. 单元测试 - Service层
@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void shouldCreateUser() {
        when(userRepository.save(any())).thenReturn(new User());
        User user = userService.register("test", "password", "test@example.com");
        assertNotNull(user);
    }
}

// 2. 集成测试 - 多层协作
@SpringBootTest
@Transactional
class OrderIntegrationTest {
    @Autowired
    private OrderService orderService;

    @Test
    void shouldCreateOrderEndToEnd() {
        Order order = orderService.createOrder(request);
        assertNotNull(order.getId());
    }
}

// 3. Controller测试
@WebMvcTest(UserController.class)
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    void shouldReturnUser() throws Exception {
        when(userService.getUserById(1L)).thenReturn(new User());
        mockMvc.perform(get("/api/users/1"))
            .andExpect(status().isOk());
    }
}
```

### 10. 什么时候应该使用分层架构?

**适合**:
- 中小型Web应用
- 传统企业应用
- 团队熟悉的模式
- 业务逻辑中等复杂

**不适合**:
- 极其简单的CRUD
- 复杂领域模型(考虑DDD)
- 大规模分布式(考虑微服务)

## 七、实战案例:电商订单系统

### 完整示例

```java
// 1. Controller
@RestController
@RequestMapping("/api/orders")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderDTO> createOrder(@RequestBody CreateOrderRequest request) {
        Order order = orderService.createOrder(request);
        return ResponseEntity.ok(toDTO(order));
    }
}

// 2. Service
@Service
@Transactional
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductService productService;

    public Order createOrder(CreateOrderRequest request) {
        // 业务逻辑
        validateOrder(request);
        Order order = buildOrder(request);
        productService.reduceStock(request.getItems());
        return orderRepository.save(order);
    }
}

// 3. Repository
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserId(Long userId);
}

// 4. Entity
@Entity
public class Order {
    @Id
    @GeneratedValue
    private Long id;
    private Long userId;
    private BigDecimal totalAmount;
    private OrderStatus status;
}
```

## 八、总结

分层架构的关键要点:
1. **职责分离**: 每层明确职责
2. **单向依赖**: 上层依赖下层
3. **接口隔离**: 通过接口解耦
4. **适度分层**: 避免过度设计
5. **经典模式**: 易于理解和实施
