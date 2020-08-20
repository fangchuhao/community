package com.example.demo.entity;

/**
 * 分页类
 */
public class Page {
    private int currentPage=1;
    private int limit=10;
    private String url;
    private int total;

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }
    public int getOffet() {
        return (currentPage-1)*limit;
    }
    public int getTotalPage() {
        int i = total / limit;
        return total%limit==0?i:i+1;
    }
    public int getFrom() {
        int i = currentPage - 2;
        return i>0?i:1;
    }
    public int getTo() {
        int i = currentPage + 2;
        return i<getTotalPage()?i:getTotalPage();
    }

    @Override
    public String toString() {
        return "Page{" +
                "currentPage=" + currentPage +
                ", limit=" + limit +
                ", url='" + url + '\'' +
                ", total=" + total +
                '}';
    }
}
