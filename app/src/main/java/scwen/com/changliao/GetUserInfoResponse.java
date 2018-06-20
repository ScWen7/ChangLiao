package scwen.com.changliao;

/**
 * Created by xxh on 2018/6/15.
 */

public class GetUserInfoResponse extends  BaseResult {


    /**
     * data : {"nickname":"测试员","realname":"测试员","userimg":"180211"}
     */

    private DataBean data;

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public static class DataBean {
        /**
         * nickname : 测试员
         * realname : 测试员
         * userimg : 180211
         */

        private String nickname;
        private String realname;
        private String userimg;

        public String getNickname() {
            return nickname;
        }

        public void setNickname(String nickname) {
            this.nickname = nickname;
        }

        public String getRealname() {
            return realname;
        }

        public void setRealname(String realname) {
            this.realname = realname;
        }

        public String getUserimg() {
            return userimg;
        }

        public void setUserimg(String userimg) {
            this.userimg = userimg;
        }
    }
}
