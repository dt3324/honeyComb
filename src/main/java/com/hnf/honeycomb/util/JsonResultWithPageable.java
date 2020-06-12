package com.hnf.honeycomb.util;

import java.util.List;

/**
 * @author admin
 */
public class JsonResultWithPageable<T> extends JsonResult<List<T>> {
    private static final long serialVersionUID = 0x10L;

    private final Integer totalPage;
    private final Long count;

    private JsonResultWithPageable(List<T> ts, Integer totalPage, Long count) {
        super(ts);
        super.setData(ts);
        this.totalPage = totalPage;
        this.count = count;
    }

    public static <T> JsonResultWithPageable<T> succeed(List<T> list, Integer totalPage, Long count) {
        return new JsonResultWithPageable<>(list, totalPage, count);
    }

    public Integer getTotalPage() {
        return totalPage;
    }

    public Long getCount() {
        return count;
    }

    @Override
    @Deprecated
    public JsonResultWithPageable setData(List<T> data) {
        throw new UnsupportedOperationException("ViewObject应遵循不可变原则");
    }
}
