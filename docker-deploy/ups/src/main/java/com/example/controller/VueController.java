package com.example.controller;

import com.example.model.Order;
import com.example.model.Truck;
import com.example.model.User;
import com.example.service.OrderService;
import com.example.service.TruckService;
import com.example.service.UserService;
import com.example.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.bind.annotation.*;
import com.example.utils.PasswordUtil;
import org.springframework.web.servlet.view.RedirectView;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;

//This controller maps the root path ("/") to the index.html file generated by the Vue.js build.
@Controller
public class VueController {

    private final JavaMailSender emailSender;
    private final UserService userService;
    private final TruckService truckService;
    private final OrderService orderService;
    private final PlatformTransactionManager transactionManager;

    @Autowired
    public VueController(UserService userService, TruckService truckService, OrderService orderService, JavaMailSender emailSender, PlatformTransactionManager transactionManager) {
        this.userService = userService;
        this.truckService = truckService;
        this.orderService = orderService;
        this.emailSender = emailSender;
        this.transactionManager = transactionManager;
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/mylogin")
    public String mylogin() {
        return "index";
    }


    @GetMapping("/changepwd")
    public RedirectView changepwd() {
        return new RedirectView("/");
    }

    @GetMapping("/tracking")
    public String tracking() {
        return "index";
    }

    @PostMapping("/tracking")
    public ResponseEntity<List<Order>> trackOrder(@RequestBody Map<String, String> request) {
        String shipId = request.get("shipId");
        System.out.println("Received shipId in tracking: " + shipId);
        Order myorder;
        List<Order> orders = new ArrayList<>(1);
        if (shipId != null && !shipId.isEmpty()) {
            myorder = orderService.getOrderByShipId(Long.valueOf(shipId));
            if (myorder != null) {
                orders.add(myorder);
            }
        }
        return ResponseEntity.ok(orders);
    }

    @PostMapping("/orderdetail")
    public ResponseEntity<Order> orderDetail
            (@RequestBody Map<String, String> request) {
        // edit_mode = 0; query the order
        // edit_mode = 1; update the order
        // edit_mode = 2; cancel the order
        String shipId = request.get("shipId");
        String mode = request.get("edit_mode");
        System.out.println("=======================");
        System.out.println("Received shipId in order detail: " + shipId);
        System.out.println("edit mode is before judge" + mode);
        if (mode.equals("1")) {
            // TODO: handle destination change
            String newX = request.get("newX");
            String newY = request.get("newY");
            System.out.println("edit mode is 1");
            System.out.println("X: " + newX);
            System.out.println("Y: " + newY);
            DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
            TransactionStatus status = transactionManager.getTransaction(definition);
            //TODO: test with success
            try {
                Order myorder = orderService.getOrderByShipId(Long.valueOf(shipId));
                String orderStatus = myorder.getShipmentStatus();
                boolean ifEdit = orderStatus.equals("created") || orderStatus.equals("truck en route to warehouse") || orderStatus.equals("truck waiting for package");
                if (ifEdit) {
                    // if order can be change -> update database -> return new order info
                    myorder.setX(Integer.parseInt(newX));
                    myorder.setY(Integer.parseInt(newY));
                    orderService.updateOrder(myorder);
//                    //test
//                    Order myNeworder = orderService.getOrderByShipId(Long.valueOf(shipId));
//                    System.out.println("new x in order is " + myNeworder.getX());
                    transactionManager.commit(status);
                    return ResponseEntity.ok(myorder);
                } else {
                    // if cannot be changed -> return old order info and error to change it
                    Order orderEdit = new Order(0L, myorder.getUserId(), myorder.getShipmentStatus(), myorder.getId(), myorder.getDescription(), myorder.getX(), myorder.getY(), myorder.getWhId(), myorder.getCreatedTime());
                    // need to check if zero
                    orderEdit.setTruckId(myorder.getTruckId());
                    orderEdit.setDeliveringTime(myorder.getDeliveringTime());
                    orderEdit.setDeliveredTime(myorder.getDeliveredTime());
                    transactionManager.commit(status);
                    return ResponseEntity.ok(orderEdit);
                }
            } catch (Exception e) {
                // Roll back the transaction in case of an error
                transactionManager.rollback(status);
                throw e;
            }

        } else if (mode.equals("0")) {
            // TODO: give order information, no success or failure
            System.out.println("edit mode is 0");
            Order myorder = orderService.getOrderByShipId(Long.valueOf(shipId));
            return ResponseEntity.ok(myorder);
        } else {
            System.out.println("edit mode is 2");
            //TODO: function not ready
            Boolean ifCancel = false;
            if (ifCancel) {
                // if cancel order success -> get data info -> delete database -> redirect to home page
                Order myorder = orderService.getOrderByShipId(Long.valueOf(shipId));
                return ResponseEntity.ok(myorder);
            } else {
                // if cancel order failure
                Order orderCancel = new Order(0L, "delivering", 1L, "description1", 11, 11, 1, LocalDateTime.now());
                return ResponseEntity.ok(orderCancel);
            }

        }
    }

    //TODO: maybe delete
    @PostMapping("/editorderdetail")
    public ResponseEntity<Order> editorderDetail
    (@RequestBody Map<String, String> request) {
        String shipId = request.get("shipId");
        System.out.println("Received shipId in edit order detail: " + shipId);
        Order myorder = orderService.getOrderByShipId(Long.valueOf(shipId));
        return ResponseEntity.ok(myorder);
    }


    @GetMapping("/myorder")
    public ResponseEntity<List<Order>> myorder
            (@RequestHeader(value = "Authorization", required = false) String authHeader) throws SQLException {
        if (authHeader == null) {
            //redirect
            return ResponseEntity.status(HttpStatus.FOUND).header(HttpHeaders.LOCATION, "/mylogin").build();
        }
        String authToken = authHeader.replace("Bearer ", "");
        String userId = JwtUtil.getUsernameFromToken(authToken);

        List<Order> orders = userService.getOrdersByUserId(userId);
        return ResponseEntity.ok(orders);
    }


    @GetMapping("/test")
    public String test() throws SQLException {
        //TODO: for debug

        Order order1 = new Order(11L, "delivering", 1L, "description1", 11, 11, 1, LocalDateTime.now());
        Order order2 = new Order(22L, "sasa", "created", 2L, "description2", 22, 22, 2, LocalDateTime.now());
        orderService.addOrder(order1);
        orderService.addOrder(order2);
        order2 = new Order(23L, "sasa", "created", 2L, "description2", 22, 22, 2, LocalDateTime.now());
        orderService.addOrder(order2);
        order2 = new Order(24L, "sasa", "created", 2L, "description2", 22, 22, 2, LocalDateTime.now());
        orderService.addOrder(order2);
        order2 = new Order(25L, "sasa", "created", 2L, "description2", 22, 22, 2, LocalDateTime.now());
        orderService.addOrder(order2);
        order2 = new Order(26L, "sasa", "created", 2L, "description2", 22, 22, 2, LocalDateTime.now());
        orderService.addOrder(order2);
        order2 = new Order(27L, "sasa", "created", 2L, "description2", 22, 22, 2, LocalDateTime.now());
        orderService.addOrder(order2);
        order2 = new Order(28L, "sasa", "created", 2L, "description2", 22, 22, 2, LocalDateTime.now());
        orderService.addOrder(order2);
        order2 = new Order(29L, "sasa", "created", 2L, "description2", 22, 22, 2, LocalDateTime.now());
        orderService.addOrder(order2);
        order2 = new Order(30L, "sasa", "created", 2L, "description2", 22, 22, 2, LocalDateTime.now());
        orderService.addOrder(order2);
        order2 = new Order(31L, "sasa", "created", 2L, "description2", 22, 22, 2, LocalDateTime.now());
        orderService.addOrder(order2);
        order2 = new Order(32L, "sasa", "created", 2L, "description2", 22, 22, 2, LocalDateTime.now());
        orderService.addOrder(order2);
        order2 = new Order(33L, "sasa", "created", 2L, "description2", 22, 22, 2, LocalDateTime.now());
        orderService.addOrder(order2);
        Truck truck = new Truck(1, "idle", 1, 2);
        truckService.addTruck(truck);

        //update
        order1.setUserId("sasa");
        order1.setTruckId(1);
        orderService.updateOrder(order1);


        return "welcome";
    }


    @GetMapping("/signup")
    public String signup() {
        return "index";
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signUp(@RequestBody Map<String, String> req) {
        String userId = req.get("userId");
        String email = req.get("email");
        String password = req.get("password");
        System.out.println("Received user: " + userId);
        try {
// String salt = PasswordUtil.generateSalt();
            String hashedPassword = PasswordUtil.hashPassword(password, "saltECE568love");
            User myuser = new User(userId, email, hashedPassword);
// myuser.setSalt(salt);
            userService.addUser(myuser);
// after signup, login directly and save account into JWT
            String token = JwtUtil.generateToken(userId);
            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("token", token);
            responseBody.put("userInfo", myuser);

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("temp_for_project@outlook.com");
            message.setTo(email);
            message.setSubject("SignUp for mini UPS.");
            String textBody = "Dear " + userId + ",\n\n"
                    + "Welcome to Mini UPS! We appreciate your decision to sign up with us.\n\n"
                    + "Best regards,\n\n" + "mini-ups-team";
            message.setText(textBody);
            //TODO: send signup email, remove the comment when upload
            emailSender.send(message);
            return ResponseEntity.ok(responseBody);
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Oops, something wrong sad.");
        }
    }

    @PostMapping("/mylogin")
    public ResponseEntity<?> login(@RequestBody Map<String, String> req) {
        String userId = req.get("userId");
        String password = req.get("password");
        System.out.println("Login user: " + userId);
        User myuser = userService.getUserById(userId);
        if (myuser != null) {
            String hashedPassword = PasswordUtil.hashPassword(password, "saltECE568love");
            if (myuser.getPassword().equals(hashedPassword)) {
                // login successfully, create and save JWT,session or Token
                //1. login successfully, create JWT
                String token = JwtUtil.generateToken(userId);
                //2. create JSON obj, including JWT
                Map<String, Object> responseBody = new HashMap<>();
                responseBody.put("token", token);
                responseBody.put("userInfo", myuser);
                //3. return JSON obj
                return ResponseEntity.ok(responseBody);
            } else {
                System.out.println("Password is not same");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid password");
            }
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<?> profile(@RequestHeader(value = "Authorization") String authHeader) {
        String authToken = authHeader.replace("Bearer ", "");
        String userId = JwtUtil.getUsernameFromToken(authToken);
        System.out.println("Look at profile of: " + userId);
        User myuser = userService.getUserById(userId);
        if (myuser != null) {
            String email = myuser.getEmail();
            return ResponseEntity.ok(email);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
        }
    }

    @PostMapping("/profile")
    public ResponseEntity<?> editProfile(@RequestHeader(value = "Authorization") String authHeader,
                                         @RequestBody Map<String, String> req) throws SQLException {
        String newEmail = req.get("newEmail");
        String authToken = authHeader.replace("Bearer ", "");
        String userId = JwtUtil.getUsernameFromToken(authToken);
        User myuser = userService.getUserById(userId);

        if (myuser != null) {
            myuser.setEmail(newEmail);
            userService.updateUser(myuser);
            return ResponseEntity.ok(myuser.getEmail());
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
        }
    }

    @GetMapping("/logout")
    public RedirectView logout() {
        return new RedirectView("/", true);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        System.out.println("Now doing logout");
        String headerAuth = request.getHeader("Authorization");

        // Check if headerAuth is not null before calling substring()
        if (headerAuth != null && headerAuth.startsWith("Bearer ")) {
            String jwtToken = headerAuth.substring(7);
            String userId = JwtUtil.getUsernameFromToken(jwtToken);
            System.out.println("User want to log out: " + userId);
            return ResponseEntity.ok(userId);
        } else {
            // Handle the case where the Authorization header is missing or invalid
            System.out.println("Authorization header is missing or invalid");
            // You might want to return a different response or throw an exception here
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authorization header is missing or invalid");
        }
    }

    @PostMapping("/changepwd")
    public ResponseEntity<?> changepwd(@RequestBody Map<String, String> request, @RequestHeader("Authorization") String authHeader) {
        System.out.println("Now doing change pwd");
        String oldPassword = request.get("oldPassword");
        String oldHashedPassword = PasswordUtil.hashPassword(oldPassword, "saltECE568love");
        String newPassword = request.get("newPassword");
        String newHashedPassword = PasswordUtil.hashPassword(newPassword, "saltECE568love");

        String authToken = authHeader.replace("Bearer ", "");
        String userId = JwtUtil.getUsernameFromToken(authToken);
        User myuser = userService.getUserById(userId);

        if (myuser.getPassword().equals(oldHashedPassword)) {
            System.out.println("old pwd: correct");
            myuser.setPassword(newHashedPassword);
            try {
                userService.updateUser(myuser);
                System.out.println("new pwd: done");
                return ResponseEntity.ok("Password changed successfully.");
            } catch (SQLException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Oops, database wrong sad.");
            }
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Old password is not correct");
        }
    }
}
