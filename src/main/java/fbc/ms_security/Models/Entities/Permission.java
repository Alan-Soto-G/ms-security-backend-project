package fbc.ms_security.Models.Entities;

import org.springframework.data.annotation.Id;

public class Permission {
    @Id
    private String _id;
    private String url;
    private String method;
    private String model;
    private Integer usageCount = 0;

    public Permission() {}

    public Permission(String url, String method) {
        this.url = url;
        this.method = method;
        this.usageCount = 0;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Integer getUsageCount() {
        return usageCount;
    }

    public void setUsageCount(Integer usageCount) {
        this.usageCount = usageCount;
    }

    public void incrementUsage() {
        this.usageCount++;
    }
}
