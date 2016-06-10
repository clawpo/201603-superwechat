package cn.ucai.superwechat.bean;

import java.io.Serializable;

public class MemberUserAvatar extends UserAvatar implements Serializable {
	private Integer mmemberId;
//	private String mmemberUserName;
	private Integer mmemberGroupId;
	private String mmemberGroupHxid;
	private Integer mmemberPermission;
	public MemberUserAvatar() {
		super();
	}

	public MemberUserAvatar(String muserName, String muserNick, Integer mavatarId, String mavatarPath,
			Integer mavatarType, String mavatarLastUpdateTime,Integer mmemberId, Integer mmemberGroupId, String mmemberGroupHxid,
			Integer mmemberPermission) {
		super(muserName, muserNick, mavatarId, mavatarPath, mavatarType, mavatarLastUpdateTime);
		this.mmemberId = mmemberId;
		this.mmemberGroupId = mmemberGroupId;
		this.mmemberGroupHxid = mmemberGroupHxid;
		this.mmemberPermission = mmemberPermission;
	}

	public Integer getMMemberId() {
		return mmemberId;
	}
	public void setMMemberId(Integer mmemberId) {
		this.mmemberId = mmemberId;
	}
/*	public String getMMemberUserName() {
		return mmemberUserName;
	}
	public void setMMemberUserName(String mmemberUserName) {
		this.mmemberUserName = mmemberUserName;
	}*/
	public Integer getMMemberGroupId() {
		return mmemberGroupId;
	}
	public void setMMemberGroupId(Integer mmemberGroupId) {
		this.mmemberGroupId = mmemberGroupId;
	}
	public String getMMemberGroupHxid() {
		return mmemberGroupHxid;
	}
	public void setMMemberGroupHxid(String mmemberGroupHxid) {
		this.mmemberGroupHxid = mmemberGroupHxid;
	}
	public Integer getMMemberPermission() {
		return mmemberPermission;
	}
	public void setMMemberPermission(Integer mmemberPermission) {
		this.mmemberPermission = mmemberPermission;
	}
	@Override
	public String toString() {
		return "MemberUserAvatar [mmemberId=" + mmemberId + ", mmemberGroupId=" + mmemberGroupId + ", mmemberGroupHxid="
				+ mmemberGroupHxid + ", mmemberPermission=" + mmemberPermission + "]";
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof MemberUserAvatar)) return false;
		if (!super.equals(o)) return false;

		MemberUserAvatar that = (MemberUserAvatar) o;

		return  (getMUserName().equals(that.getMUserName())
            && mmemberGroupId.equals(that.mmemberGroupId)
		    && mmemberGroupHxid.equals(that.mmemberGroupHxid));

	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
        result = 31 * result + getMUserName().hashCode();
		result = 31 * result + mmemberGroupId.hashCode();
		result = 31 * result + mmemberGroupHxid.hashCode();
		return result;
	}
}
