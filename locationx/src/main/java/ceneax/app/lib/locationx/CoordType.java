package ceneax.app.lib.locationx;

public enum CoordType {
    /**
     * 地球坐标系，国际通用坐标系，国际标准
     * <br/>
     * 获取方式：从GPS设备中获取的坐标数据
     * <br/>
     * 应用场合：国际地图提供商，谷歌国际地图
     */
    WGS84,
    /**
     * 火星坐标系，WGS84坐标系加密后的坐标系，中国标准
     * <br/>
     * 获取方式：从国行移动设备中定位获取的坐标数据
     * <br/>
     * 应用场合：高德地图，谷歌地图，腾讯地图等
     */
    GCJ02,
    /**
     * 百度坐标系，百度标准
     * <br/>
     * 获取方式：百度在火星GCJ02坐标基础上二次加密后的坐标数据
     * <br/>
     * 应用场合：百度地图
     */
    BD09
}