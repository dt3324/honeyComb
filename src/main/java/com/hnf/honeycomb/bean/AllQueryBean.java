package com.hnf.honeycomb.bean;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * 全文检索
 *
 * @author 佚名
 */
public class AllQueryBean implements Serializable {
    private static final long serialVersionUID = -205034799093733107L;
    /**
     * id
     */
    private Long id;

    /**
     * 内容
     */
    private String conTent;

    /**
     * 时间
     */
    private Long time;

    /**
     * 数据类型
     */
    private Long idxType;

    /**
     * 索引id
     */
    private Long idxId;

    private Long countIdType;

    private String nickname;

    private String recvName;

    private Long msgTime;

    private String senderName;

    private String name;

    private String phone;

    private String telePhone;

    private String uniseq;

    private String deviceUnique;

    private Date msgDate;

    private String caseUnique;

    public AllQueryBean() {
    }

    public Long getId() {
        return id;
    }

    public AllQueryBean setId(Long id) {
        this.id = id;
        return this;
    }

    public String getConTent() {
        return conTent;
    }

    public AllQueryBean setConTent(String conTent) {
        this.conTent = conTent;
        return this;
    }

    public Long getTime() {
        return time;
    }

    public AllQueryBean setTime(Long time) {
        this.time = time;
        return this;
    }

    public Long getIdxType() {
        return idxType;
    }

    public AllQueryBean setIdxType(Long idxType) {
        this.idxType = idxType;
        return this;
    }

    public Long getIdxId() {
        return idxId;
    }

    public AllQueryBean setIdxId(Long idxId) {
        this.idxId = idxId;
        return this;
    }

    public Long getCountIdType() {
        return countIdType;
    }

    public AllQueryBean setCountIdType(Long countIdType) {
        this.countIdType = countIdType;
        return this;
    }

    public String getNickname() {
        return nickname;
    }

    public AllQueryBean setNickname(String nickname) {
        this.nickname = nickname;
        return this;
    }

    public String getRecvName() {
        return recvName;
    }

    public AllQueryBean setRecvName(String recvName) {
        this.recvName = recvName;
        return this;
    }

    public Long getMsgTime() {
        return msgTime;
    }

    public AllQueryBean setMsgTime(Long msgTime) {
        this.msgTime = msgTime;
        return this;
    }

    public String getSenderName() {
        return senderName;
    }

    public AllQueryBean setSenderName(String senderName) {
        this.senderName = senderName;
        return this;
    }

    public String getName() {
        return name;
    }

    public AllQueryBean setName(String name) {
        this.name = name;
        return this;
    }

    public String getPhone() {
        return phone;
    }

    public AllQueryBean setPhone(String phone) {
        this.phone = phone;
        return this;
    }

    public String getTelePhone() {
        return telePhone;
    }

    public AllQueryBean setTelePhone(String telePhone) {
        this.telePhone = telePhone;
        return this;
    }

    public String getUniseq() {
        return uniseq;
    }

    public AllQueryBean setUniseq(String uniseq) {
        this.uniseq = uniseq;
        return this;
    }

    public String getDeviceUnique() {
        return deviceUnique;
    }

    public AllQueryBean setDeviceUnique(String deviceUnique) {
        this.deviceUnique = deviceUnique;
        return this;
    }

    public Date getMsgDate() {
        return msgDate;
    }

    public AllQueryBean setMsgDate(Date msgDate) {
        this.msgDate = msgDate;
        return this;
    }

    public String getCaseUnique() {
        return caseUnique;
    }

    public AllQueryBean setCaseUnique(String caseUnique) {
        this.caseUnique = caseUnique;
        return this;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("\"id\":")
                .append(id);
        sb.append(",\"conTent\":\"")
                .append(conTent).append('\"');
        sb.append(",\"time\":")
                .append(time);
        sb.append(",\"idxType\":")
                .append(idxType);
        sb.append(",\"idxId\":")
                .append(idxId);
        sb.append(",\"countIdType\":")
                .append(countIdType);
        sb.append(",\"nickname\":\"")
                .append(nickname).append('\"');
        sb.append(",\"recvName\":\"")
                .append(recvName).append('\"');
        sb.append(",\"msgTime\":")
                .append(msgTime);
        sb.append(",\"senderName\":\"")
                .append(senderName).append('\"');
        sb.append(",\"name\":\"")
                .append(name).append('\"');
        sb.append(",\"phone\":\"")
                .append(phone).append('\"');
        sb.append(",\"telePhone\":\"")
                .append(telePhone).append('\"');
        sb.append(",\"uniseq\":\"")
                .append(uniseq).append('\"');
        sb.append(",\"deviceUnique\":\"")
                .append(deviceUnique).append('\"');
        sb.append(",\"msgDate\":\"")
                .append(msgDate).append('\"');
        sb.append(",\"caseUnique\":\"")
                .append(caseUnique).append('\"');
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AllQueryBean that = (AllQueryBean) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(conTent, that.conTent) &&
                Objects.equals(time, that.time) &&
                Objects.equals(idxType, that.idxType) &&
                Objects.equals(idxId, that.idxId) &&
                Objects.equals(countIdType, that.countIdType) &&
                Objects.equals(nickname, that.nickname) &&
                Objects.equals(recvName, that.recvName) &&
                Objects.equals(msgTime, that.msgTime) &&
                Objects.equals(senderName, that.senderName) &&
                Objects.equals(name, that.name) &&
                Objects.equals(phone, that.phone) &&
                Objects.equals(telePhone, that.telePhone) &&
                Objects.equals(uniseq, that.uniseq) &&
                Objects.equals(deviceUnique, that.deviceUnique) &&
                Objects.equals(msgDate, that.msgDate) &&
                Objects.equals(caseUnique, that.caseUnique);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, conTent, time, idxType, idxId, countIdType, nickname, recvName, msgTime, senderName, name, phone, telePhone, uniseq, deviceUnique, msgDate, caseUnique);
    }
}
