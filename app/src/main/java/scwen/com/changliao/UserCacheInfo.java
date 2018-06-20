package scwen.com.changliao;

import org.litepal.crud.LitePalSupport;

/**
 * Created by xxh on 2018/6/11.
 */

public class UserCacheInfo extends LitePalSupport{


    private String uid;

    private String nickname;

    private String headurl;


    public String getUid() {
        return uid == null ? "" : uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getNickname() {
        return nickname == null ? "" : nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getHeadurl() {
        return headurl == null ? "" : headurl;
    }

    public void setHeadurl(String headurl) {
        this.headurl = headurl;
    }
}
