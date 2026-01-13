package com.architecture.datastructure.advanced.trie;

import java.util.HashMap;
import java.util.Map;

/**
 * 字典树 - Trie (Prefix Tree)
 * 用于高效存储和检索字符串
 */
public class TrieImplementation {

    static class TrieNode {
        Map<Character, TrieNode> children;
        boolean isEndOfWord;

        TrieNode() {
            children = new HashMap<>();
            isEndOfWord = false;
        }
    }

    private TrieNode root;

    public TrieImplementation() {
        root = new TrieNode();
    }

    /** 插入单词 - O(m), m是单词长度 */
    public void insert(String word) {
        TrieNode node = root;
        for (char c : word.toCharArray()) {
            node.children.putIfAbsent(c, new TrieNode());
            node = node.children.get(c);
        }
        node.isEndOfWord = true;
    }

    /** 搜索单词 - O(m) */
    public boolean search(String word) {
        TrieNode node = findNode(word);
        return node != null && node.isEndOfWord;
    }

    /** 前缀搜索 - O(m) */
    public boolean startsWith(String prefix) {
        return findNode(prefix) != null;
    }

    private TrieNode findNode(String word) {
        TrieNode node = root;
        for (char c : word.toCharArray()) {
            if (!node.children.containsKey(c)) {
                return null;
            }
            node = node.children.get(c);
        }
        return node;
    }
}
