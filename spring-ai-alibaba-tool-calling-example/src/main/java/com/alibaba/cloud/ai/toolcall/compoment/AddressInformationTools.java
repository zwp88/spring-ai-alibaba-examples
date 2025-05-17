package com.alibaba.cloud.ai.toolcall.compoment;

import com.alibaba.cloud.ai.toolcalling.baidumap.BaiduMapSearchInfoService;

public class AddressInformationTools {

    private final BaiduMapSearchInfoService service;

    public AddressInformationTools(BaiduMapSearchInfoService service) {
        this.service = service;
    }

    public String getAddressInformation(String address) {
        return service.apply(new BaiduMapSearchInfoService.Request(address)).message();
    }

}
