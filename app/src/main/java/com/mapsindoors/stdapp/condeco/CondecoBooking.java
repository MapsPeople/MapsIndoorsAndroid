
package com.mapsindoors.stdapp.condeco;

import java.util.Date;

import com.google.gson.annotations.SerializedName;

public class CondecoBooking {

    @SerializedName("bookingID")
    private int bookingID;
    @SerializedName("roomID")
    private int roomID;
    @SerializedName("locationId")
    private int locationId;
    @SerializedName("locationTimeZone")
    private String locationTimeZone;
    @SerializedName("meetingTitle")
    private String meetingTitle;
    @SerializedName("timeFrom")
    private Date timeFrom;
    @SerializedName("timeTo")
    private Date timeTo;

    @SerializedName("timeFromUTC")
    private String timeFromUTC;
    @SerializedName("timeToUTC")
    private String timeToUTC;

    private Date timeFromUTCDate;
    private Date timeToUTCDate;


    @SerializedName("utcWithSetupTime")
    private Date utcWithSetupTime;
    @SerializedName("utcWithCleanDownTime")
    private Date utcWithCleanDownTime;
    @SerializedName("actualTimeFrom")
    private Date actualTimeFrom;
    @SerializedName("actualTimeTo")
    private Date actualTimeTo;
    @SerializedName("bookingRealStartTime")
    private Date bookingRealStartTime;
    @SerializedName("bookingRealEndTime")
    private Date bookingRealEndTime;
    @SerializedName("hostName")
    private String hostName;
    @SerializedName("hostEmail")
    private String hostEmail;
    @SerializedName("meetingProgress")
    private int meetingProgress;
    @SerializedName("prepUnit")
    private int prepUnit;
    @SerializedName("prepPeriod")
    private int prepPeriod;
    @SerializedName("cleanDownUnit")
    private int cleanDownUnit;
    @SerializedName("cleanDownPeriod")
    private int cleanDownPeriod;
    @SerializedName("roomClear")
    private boolean roomClear;
    @SerializedName("instantMeeting")
    private boolean instantMeeting;
    @SerializedName("pendingOnGrid")
    private boolean pendingOnGrid;
    @SerializedName("privateMeeting")
    private boolean privateMeeting;
    @SerializedName("lastModifiedOn")
    private Date lastModifiedOn;
    @SerializedName("modifiedBy")
    private int modifiedBy;
    @SerializedName("createdOn")
    private Date createdOn;
    @SerializedName("createdBy")
    private int createdBy;
    @SerializedName("deleted")
    private int deleted;
    @SerializedName("recurranceId")
    private int recurranceId;
    @SerializedName("waitList")
    private int waitList;
    @SerializedName("dateFrom")
    private Date dateFrom;
    @SerializedName("groupId")
    private int groupId;
    @SerializedName("bookingStart")
    private Date bookingStart;
    @SerializedName("bookingEnd")
    private Date bookingEnd;
    @SerializedName("bookingSource")
    private int bookingSource;
    @SerializedName("userRole")
    private CondecoBookingUserRole userRole;
    @SerializedName("status")
    private CondecoBookingStatus status;
    @SerializedName("hostId")
    private int hostId;
    @SerializedName("requestorId")
    private int requestorId;
    @SerializedName("requestorName")
    private String requestorName;
    @SerializedName("requestorEmail")
    private String requestorEmail;
    @SerializedName("creatorName")
    private String creatorName;
    @SerializedName("creatorEmail")
    private String creatorEmail;
    @SerializedName("setupStyle")
    private String setupStyle;
    @SerializedName("notes")
    private String notes;
    @SerializedName("meetingType")
    private String meetingType;

    public int getBookingID() {
        return bookingID;
    }

    public void setBookingID(int bookingID) {
        this.bookingID = bookingID;
    }

    public int getRoomID() {
        return roomID;
    }

    public void setRoomID(int roomID) {
        this.roomID = roomID;
    }

    public int getLocationId() {
        return locationId;
    }

    public void setLocationId(int locationId) {
        this.locationId = locationId;
    }

    public String getLocationTimeZone() {
        return locationTimeZone;
    }

    public void setLocationTimeZone(String locationTimeZone) {
        this.locationTimeZone = locationTimeZone;
    }

    public String getMeetingTitle() {
        return meetingTitle;
    }

    public void setMeetingTitle(String meetingTitle) {
        this.meetingTitle = meetingTitle;
    }

    public Date getTimeFrom() {
        return timeFrom;
    }

    public void setTimeFrom(Date timeFrom) {
        this.timeFrom = timeFrom;
    }

    public Date getTimeTo() {
        return timeTo;
    }

    public void setTimeTo(Date timeTo) {
        this.timeTo = timeTo;
    }

    public String getTimeFromUTC() {
        return timeFromUTC;
    }

    public void setTimeFromUTC(String timeFromUTC) {
        this.timeFromUTC = timeFromUTC;
    }

    public String getTimeToUTC() {
        return timeToUTC;
    }

    public void setTimeToUTC(String timeToUTC) {
        this.timeToUTC = timeToUTC;
    }

    public Date getUtcWithSetupTime() {
        return utcWithSetupTime;
    }

    public void setUtcWithSetupTime(Date utcWithSetupTime) {
        this.utcWithSetupTime = utcWithSetupTime;
    }

    public Date getUtcWithCleanDownTime() {
        return utcWithCleanDownTime;
    }

    public void setUtcWithCleanDownTime(Date utcWithCleanDownTime) {
        this.utcWithCleanDownTime = utcWithCleanDownTime;
    }

    public Date getActualTimeFrom() {
        return actualTimeFrom;
    }

    public void setActualTimeFrom(Date actualTimeFrom) {
        this.actualTimeFrom = actualTimeFrom;
    }

    public Date getActualTimeTo() {
        return actualTimeTo;
    }

    public void setActualTimeTo(Date actualTimeTo) {
        this.actualTimeTo = actualTimeTo;
    }

    public Date getBookingRealStartTime() {
        return bookingRealStartTime;
    }

    public void setBookingRealStartTime(Date bookingRealStartTime) {
        this.bookingRealStartTime = bookingRealStartTime;
    }

    public Date getBookingRealEndTime() {
        return bookingRealEndTime;
    }

    public void setBookingRealEndTime(Date bookingRealEndTime) {
        this.bookingRealEndTime = bookingRealEndTime;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getHostEmail() {
        return hostEmail;
    }

    public void setHostEmail(String hostEmail) {
        this.hostEmail = hostEmail;
    }

    public int getMeetingProgress() {
        return meetingProgress;
    }

    public void setMeetingProgress(int meetingProgress) {
        this.meetingProgress = meetingProgress;
    }

    public int getPrepUnit() {
        return prepUnit;
    }

    public void setPrepUnit(int prepUnit) {
        this.prepUnit = prepUnit;
    }

    public int getPrepPeriod() {
        return prepPeriod;
    }

    public void setPrepPeriod(int prepPeriod) {
        this.prepPeriod = prepPeriod;
    }

    public int getCleanDownUnit() {
        return cleanDownUnit;
    }

    public void setCleanDownUnit(int cleanDownUnit) {
        this.cleanDownUnit = cleanDownUnit;
    }

    public int getCleanDownPeriod() {
        return cleanDownPeriod;
    }

    public void setCleanDownPeriod(int cleanDownPeriod) {
        this.cleanDownPeriod = cleanDownPeriod;
    }

    public boolean getRoomClear() {
        return roomClear;
    }

    public void setRoomClear(boolean roomClear) {
        this.roomClear = roomClear;
    }

    public boolean getInstantMeeting() {
        return instantMeeting;
    }

    public void setInstantMeeting(boolean instantMeeting) {
        this.instantMeeting = instantMeeting;
    }

    public boolean getPendingOnGrid() {
        return pendingOnGrid;
    }

    public void setPendingOnGrid(boolean pendingOnGrid) {
        this.pendingOnGrid = pendingOnGrid;
    }

    public boolean getPrivateMeeting() {
        return privateMeeting;
    }

    public void setPrivateMeeting(boolean privateMeeting) {
        this.privateMeeting = privateMeeting;
    }

    public Date getLastModifiedOn() {
        return lastModifiedOn;
    }

    public void setLastModifiedOn(Date lastModifiedOn) {
        this.lastModifiedOn = lastModifiedOn;
    }

    public int getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(int modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

    public int getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
    }

    public int getDeleted() {
        return deleted;
    }

    public void setDeleted(int deleted) {
        this.deleted = deleted;
    }

    public int getRecurranceId() {
        return recurranceId;
    }

    public void setRecurranceId(int recurranceId) {
        this.recurranceId = recurranceId;
    }

    public int getWaitList() {
        return waitList;
    }

    public void setWaitList(int waitList) {
        this.waitList = waitList;
    }

    public Date getDateFrom() {
        return dateFrom;
    }

    public void setDateFrom(Date dateFrom) {
        this.dateFrom = dateFrom;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public Date getBookingStart() {
        return bookingStart;
    }

    public void setBookingStart(Date bookingStart) {
        this.bookingStart = bookingStart;
    }

    public Date getBookingEnd() {
        return bookingEnd;
    }

    public void setBookingEnd(Date bookingEnd) {
        this.bookingEnd = bookingEnd;
    }

    public int getBookingSource() {
        return bookingSource;
    }

    public void setBookingSource(int bookingSource) {
        this.bookingSource = bookingSource;
    }

    public CondecoBookingUserRole getUserRole() {
        return userRole;
    }

    public void setUserRole(CondecoBookingUserRole userRole) {
        this.userRole = userRole;
    }

    public CondecoBookingStatus getStatus() {
        return status;
    }

    public void setStatus(CondecoBookingStatus status) {
        this.status = status;
    }

    public int getHostId() {
        return hostId;
    }

    public void setHostId(int hostId) {
        this.hostId = hostId;
    }

    public int getRequestorId() {
        return requestorId;
    }

    public void setRequestorId(int requestorId) {
        this.requestorId = requestorId;
    }

    public String getRequestorName() {
        return requestorName;
    }

    public void setRequestorName(String requestorName) {
        this.requestorName = requestorName;
    }

    public String getRequestorEmail() {
        return requestorEmail;
    }

    public void setRequestorEmail(String requestorEmail) {
        this.requestorEmail = requestorEmail;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }

    public String getCreatorEmail() {
        return creatorEmail;
    }

    public void setCreatorEmail(String creatorEmail) {
        this.creatorEmail = creatorEmail;
    }

    public String getSetupStyle() {
        return setupStyle;
    }

    public void setSetupStyle(String setupStyle) {
        this.setupStyle = setupStyle;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getMeetingType() {
        return meetingType;
    }

    public void setMeetingType(String meetingType) {
        this.meetingType = meetingType;
    }

    public Date getTimeFromUTCDate() {
        return timeFromUTCDate;
    }

    public void setTimeFromUTCDate(Date timeFromUTCDate) {
        this.timeFromUTCDate = timeFromUTCDate;
    }

    public Date getTimeToUTCDate() {
        return timeToUTCDate;
    }

    public void setTimeToUTCDate(Date timeToUTCDate) {
        this.timeToUTCDate = timeToUTCDate;
    }


}
