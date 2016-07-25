package com.dudu.obd.common;

/**
 * 登录验证类型的消息
 * @author    Bob
 * @date      2015年12月3日 上午11:52:39
 */
public class LoginMsg extends BaseMsg {

    private static final long serialVersionUID = 7583460416357708825L;

    private String            userName;

    private String            password;

    private String            obeId;

    public LoginMsg() {
        super();
        setType(MsgType.LOGIN);
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getObeId() {
        return obeId;
    }

    public void setObeId(String obeId) {
        this.obeId = obeId;
    }
}
