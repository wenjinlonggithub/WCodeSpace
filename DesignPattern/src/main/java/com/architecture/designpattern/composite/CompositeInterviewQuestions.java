package com.architecture.designpattern.composite;

import org.springframework.stereotype.Component;

@Component
public class CompositeInterviewQuestions {
    public void whatIsComposite() { System.out.println("组合模式：将对象组合成树形结构以表示部分-整体的层次结构"); }
    public void coreComponents() { System.out.println("核心组件：组件、叶子、组合"); }
    public void applicableScenarios() { System.out.println("适用场景：树形结构、文件系统、UI组件、菜单系统"); }
    public void transparentVsSafe() { System.out.println("透明方式vs安全方式：接口统一性vs类型安全性"); }
    public void recursiveOperations() { System.out.println("递归操作：遍历、查找、计算，统一处理叶子和组合"); }
}