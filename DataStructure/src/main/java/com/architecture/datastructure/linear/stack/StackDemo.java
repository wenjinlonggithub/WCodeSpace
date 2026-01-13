package com.architecture.datastructure.linear.stack;

/**
 * Stack应用演示
 */
public class StackDemo {
    public static void main(String[] args) {
        // 场景1: 括号匹配
        System.out.println("=== 括号匹配 ===");
        System.out.println(isValid("()[]{}") + " (期望: true)");
        System.out.println(isValid("([)]") + " (期望: false)");

        // 场景2: 表达式求值
        System.out.println("\n=== 逆波兰表达式 ===");
        System.out.println(evalRPN(new String[]{"2","1","+","3","*"}) + " (期望: 9)");
    }

    public static boolean isValid(String s) {
        StackImplementation<Character> stack = new StackImplementation<>();
        for (char c : s.toCharArray()) {
            if (c == '(' || c == '[' || c == '{') {
                stack.push(c);
            } else {
                if (stack.isEmpty()) return false;
                char top = stack.pop();
                if ((c == ')' && top != '(') ||
                    (c == ']' && top != '[') ||
                    (c == '}' && top != '{')) {
                    return false;
                }
            }
        }
        return stack.isEmpty();
    }

    public static int evalRPN(String[] tokens) {
        StackImplementation<Integer> stack = new StackImplementation<>();
        for (String token : tokens) {
            if (isOperator(token)) {
                int b = stack.pop();
                int a = stack.pop();
                stack.push(calculate(a, b, token));
            } else {
                stack.push(Integer.parseInt(token));
            }
        }
        return stack.pop();
    }

    private static boolean isOperator(String s) {
        return "+".equals(s) || "-".equals(s) || "*".equals(s) || "/".equals(s);
    }

    private static int calculate(int a, int b, String op) {
        return switch (op) {
            case "+" -> a + b;
            case "-" -> a - b;
            case "*" -> a * b;
            case "/" -> a / b;
            default -> 0;
        };
    }
}
