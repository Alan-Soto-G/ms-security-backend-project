package fbc.ms_security.Models.Utils;

import java.util.List;

public class MostUsedPermissionsRequest {
    private List<Integer> indices;

    public MostUsedPermissionsRequest() {}

    public MostUsedPermissionsRequest(List<Integer> indices) {
        this.indices = indices;
    }

    public List<Integer> getIndices() {
        return indices;
    }

    public void setIndices(List<Integer> indices) {
        this.indices = indices;
    }
}
