package com.example.demo.util;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Component
public class SensitiveFilter {
    private Logger logger= LoggerFactory.getLogger(SensitiveFilter.class);

    private static final String REPLACEWORDS="???";

    private TrieNode root=new TrieNode();

    @PostConstruct
    private void init() {
        loadSensitiveWord();
    }

    private class TrieNode {
        private boolean isKeyWordEnd = false; // 如果为true，则表示该节点是敏感词的结束节点

        private Map<Character,TrieNode> subNode = new HashMap<>(); // 子节点

        public boolean isKeyWordEnd() {
            return isKeyWordEnd;
        }

        public void setKeyWordEnd(boolean keyWordEnd) {
            isKeyWordEnd = keyWordEnd;
        }

        public TrieNode getSubNode(char c) {
            return subNode.get(c);
        }

        public void setSubNode(char c,TrieNode trieNode) {
            this.subNode.put(c,trieNode);
        }
    }

    public void loadSensitiveWord() {
        try(InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
            BufferedReader br=new BufferedReader(new InputStreamReader(is));) {

            String str="";
            while((str=br.readLine())!=null) {
                addKeyWord(str);
            }
        }catch (Exception e) {
            logger.error("加载敏感词汇失败！");
        }
    }

    private void addKeyWord(String str) {
        TrieNode tempNode=root;
        for(char c:str.toCharArray()) {
            TrieNode subNode = tempNode.getSubNode(c);
            if(subNode==null) {
                subNode=new TrieNode();
                tempNode.setSubNode(c,subNode);
            }
            tempNode=subNode;
        }
        tempNode.setKeyWordEnd(true);
    }

    /**
     * 判断一个字符是否是特殊符号
     * @param c
     * @return
     */
    private boolean isSymble(char c) {
        // 0x2E80-0x9FFF 东亚文字范围
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF);
    }

    /**
     * 按照sensitive-words.txt文件过滤字符串
     * @param text 待过滤的字符串
     * @return 返回过滤完的字符串
     */
    public String filter(String text) {
        if(StringUtils.isEmpty(text)) {
            throw new IllegalArgumentException("待过滤字符串不可为空值！");
        }
        StringBuilder sb=new StringBuilder();
        // 指针一，遍历前缀树
        TrieNode one = root;
        // 指针二，指向待过滤字符串中敏感词汇的第一个字符
        int two=0;
        // 指针三，指向待过滤字符串中敏感词汇的最后一个字符，会出现小幅度回调
        int three=0;

        char[] chars = text.toCharArray();
        int len=chars.length;
        while(three<len) {
            // 除去敏感词汇中间的特殊字符
            if(isSymble(chars[three])) {
                if(one==root) {
                    sb.append(chars[two]);
                    two++;
                }
                three++;
                continue;
            }

            one = one.getSubNode(chars[three]);
            if(one==null) {
                sb.append(chars[two]);
                two++;
                three++;
                one=root;
            }
            else if (one.isKeyWordEnd()) {
                sb.append(REPLACEWORDS);
                two=++three;
                one=root;
            }
            else {
                three++;
            }
        }
        sb.append(text.substring(two));
        return sb.toString();
    }
}
