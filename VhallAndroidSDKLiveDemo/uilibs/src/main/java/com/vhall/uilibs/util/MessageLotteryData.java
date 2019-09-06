package com.vhall.uilibs.util;

import com.vhall.business.MessageServer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author hkl
 * Date: 2019-07-03 17:49
 */
public class MessageLotteryData implements Serializable {

    /**
     * type : lottery_push
     * room_id : lss_7d73e0dc
     * lottery_id : 1312
     * lottery_creator_id : 1926
     * lottery_creator_avatar : //t-alistatic01.e.vhall.com/upload/user/avatar/bc/b1/bcb1c1310b1c668d0dec7bac0670f3cd.jpg
     * lottery_creator_nickname : papa
     * lottery_type : 1
     * lottery_number : 1
     * lottery_status : 0
     */
    public static final int EVENT_START_LOTTERY = 0x07;//开始抽奖消息键
    public static final int EVENT_END_LOTTERY = 0x08;//结束抽奖消息键
    private String type;
    private String room_id;
    private String lottery_id;
    private String lottery_creator_id;
    private String lottery_creator_avatar;
    private String lottery_creator_nickname;
    private String lottery_type;
    private String lottery_number;
    public int event;
    private int lottery_status;
    private List<LotteryWinnersBean> lottery_winners;


    public MessageLotteryData() {
    }

    public MessageLotteryData(String type, String room_id, String lottery_id, String lottery_creator_id, String lottery_creator_avatar, String lottery_creator_nickname, String lottery_type, String lottery_number, int event, int lottery_status) {
        this.type = type;
        this.room_id = room_id;
        this.lottery_id = lottery_id;
        this.lottery_creator_id = lottery_creator_id;
        this.lottery_creator_avatar = lottery_creator_avatar;
        this.lottery_creator_nickname = lottery_creator_nickname;
        this.lottery_type = lottery_type;
        this.lottery_number = lottery_number;
        this.event = event;
        this.lottery_status = lottery_status;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRoom_id() {
        return room_id;
    }

    public void setRoom_id(String room_id) {
        this.room_id = room_id;
    }

    public String getLottery_id() {
        return lottery_id;
    }

    public void setLottery_id(String lottery_id) {
        this.lottery_id = lottery_id;
    }

    public String getLottery_creator_id() {
        return lottery_creator_id;
    }

    public void setLottery_creator_id(String lottery_creator_id) {
        this.lottery_creator_id = lottery_creator_id;
    }

    public String getLottery_creator_avatar() {
        return lottery_creator_avatar;
    }

    public void setLottery_creator_avatar(String lottery_creator_avatar) {
        this.lottery_creator_avatar = lottery_creator_avatar;
    }

    public String getLottery_creator_nickname() {
        return lottery_creator_nickname;
    }

    public void setLottery_creator_nickname(String lottery_creator_nickname) {
        this.lottery_creator_nickname = lottery_creator_nickname;
    }

    public String getLottery_type() {
        return lottery_type;
    }

    public void setLottery_type(String lottery_type) {
        this.lottery_type = lottery_type;
    }

    public String getLottery_number() {
        return lottery_number;
    }

    public void setLottery_number(String lottery_number) {
        this.lottery_number = lottery_number;
    }

    public int getLottery_status() {
        return lottery_status;
    }

    public void setLottery_status(int lottery_status) {
        this.lottery_status = lottery_status;
    }

    public List<LotteryWinnersBean> getLottery_winners() {
        return lottery_winners;
    }

    public void setLottery_winners(List<LotteryWinnersBean> lottery_winners) {
        this.lottery_winners = lottery_winners;
    }

    public static class LotteryWinnersBean implements Serializable {
        /**
         * id : 1594
         * lottery_id : 1312
         * lottery_user_id : 16420702
         * lottery_user_nickname : 900533
         * lottery_user_avatar :
         * preset : 0
         */

        private String id;
        private String lottery_id;
        private String lottery_idX;
        private String lottery_user_id;
        private String lottery_user_nickname;
        private String lottery_user_avatar;
        private String preset;
        public boolean isSelf;

        public LotteryWinnersBean(String id, String lottery_id, String lottery_idX, String lottery_user_id, String lottery_user_nickname, String lottery_user_avatar, String preset, boolean isSelf) {
            this.id = id;
            this.lottery_id = lottery_id;
            this.lottery_idX = lottery_idX;
            this.lottery_user_id = lottery_user_id;
            this.lottery_user_nickname = lottery_user_nickname;
            this.lottery_user_avatar = lottery_user_avatar;
            this.preset = preset;
            this.isSelf = isSelf;
        }

        public LotteryWinnersBean(String id, String lottery_id, String lottery_user_nickname, boolean isSelf) {
            this.id = id;
            this.lottery_id = lottery_id;
            this.isSelf = isSelf;
            this.lottery_user_nickname = lottery_user_nickname;
        }

        public String getLottery_id() {
            return lottery_id;
        }

        public void setLottery_id(String lottery_id) {
            this.lottery_id = lottery_id;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getLottery_idX() {
            return lottery_idX;
        }

        public void setLottery_idX(String lottery_idX) {
            this.lottery_idX = lottery_idX;
        }

        public String getLottery_user_id() {
            return lottery_user_id;
        }

        public void setLottery_user_id(String lottery_user_id) {
            this.lottery_user_id = lottery_user_id;
        }

        public String getLottery_user_nickname() {
            return lottery_user_nickname;
        }

        public void setLottery_user_nickname(String lottery_user_nickname) {
            this.lottery_user_nickname = lottery_user_nickname;
        }

        public String getLottery_user_avatar() {
            return lottery_user_avatar;
        }

        public void setLottery_user_avatar(String lottery_user_avatar) {
            this.lottery_user_avatar = lottery_user_avatar;
        }

        public String getPreset() {
            return preset;
        }

        public void setPreset(String preset) {
            this.preset = preset;
        }
    }

    public static MessageLotteryData getData(MessageServer.MsgInfo messageInfo) {
        MessageLotteryData data = new MessageLotteryData();
        data.event = messageInfo.event;
        List<LotteryWinnersBean> list = new ArrayList<>();
        if (messageInfo.lotteries != null && messageInfo.lotteries.size() > 0) {
            for (MessageServer.Lottery lottery : messageInfo.lotteries) {
                LotteryWinnersBean bean = new LotteryWinnersBean(lottery.id, lottery.lottery_id, lottery.nick_name, lottery.isSelf);
                list.add(bean);
            }
            data.setLottery_winners(list);
        }
        return data;
    }
}
