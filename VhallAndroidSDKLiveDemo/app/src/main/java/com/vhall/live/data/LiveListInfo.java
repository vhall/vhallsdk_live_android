package com.vhall.live.data;

import java.util.List;

public class LiveListInfo {

    /**
     * code : 200
     * msg : 成功
     * data : {"lists":[{"user_id":"27468049","webinar_id":862081357,"subject":"huazi-h5-test","start_time":"2020-10-12 17:10:00","status":3,"desc":"test","thumb":"","pv":18,"verify":0,"topics":""}],"total":116}
     */

    public String code;
    public String msg;
    public DataBean data;

    public static class DataBean {
        /**
         * lists : [{"user_id":"27468049","webinar_id":862081357,"subject":"huazi-h5-test","start_time":"2020-10-12 17:10:00","status":3,"desc":"test","thumb":"","pv":18,"verify":0,"topics":""}]
         * total : 116
         */

        public int total;
        public List<ListsBean> lists;

        public static class ListsBean {
            /**
             * user_id : 27468049
             * webinar_id : 862081357
             * subject : huazi-h5-test
             * start_time : 2020-10-12 17:10:00
             * status : 3
             * desc : test
             * thumb :
             * pv : 18
             * verify : 0
             * topics :
             */

            public String user_id;
            public int webinar_id;
            public String subject;
            public String start_time;
            public int status;
            public String desc;
            public String thumb;
            public int pv;
            public int verify;
            public String topics;
        }
    }
}
