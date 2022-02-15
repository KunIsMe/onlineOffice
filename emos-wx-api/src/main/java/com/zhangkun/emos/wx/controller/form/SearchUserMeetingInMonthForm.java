package com.zhangkun.emos.wx.controller.form;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

@Data
@ApiModel
public class SearchUserMeetingInMonthForm {

    @Range(min = 1000, max = 4000)
    private Integer year;

    @Range(min = 1, max = 12)
    private Integer month;

}
