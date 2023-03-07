package com.vhall.uimodule.module.interactive;

import android.text.TextUtils;

import com.vhall.vhallrtc.client.Stream;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author hkl
 * Date: 2020-07-17 18:55
 */
public class StreamData {
    private Stream stream;
    private int voice = -1;
    private int camera = -1;
    private String avatar;
    private String name;
    private String role = "2";

    public StreamData(Stream stream) {
        this.stream = stream;
    }

    public Stream getStream() {
        return stream;
    }

    public void setStream(Stream stream) {
        this.stream = stream;
    }

    public String getAvatar() {
        String attributes = stream.getAttributes();
        if (!TextUtils.isEmpty(attributes)) {
            try {
                JSONObject a = new JSONObject(attributes);
                if (a != null) {
                    if (a.has("avatar")) {
                        avatar = a.optString("avatar");
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getStreamUserId() {
        if (stream!=null){
            return stream.userId;
        }
        return "";
    }

    public String getName() {
        String attributes = stream.getAttributes();
        if (!TextUtils.isEmpty(attributes)) {
            try {
                JSONObject a = new JSONObject(attributes);
                if (a != null) {
                    if (a.has("nickName")) {
                        name = a.optString("nickName");
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        String attributes = stream.getAttributes();
        if (!TextUtils.isEmpty(attributes)) {
            try {
                JSONObject a = new JSONObject(attributes);
                if (a != null) {
                    if (a.has("role")) {
                        role = a.optString("role");
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public int getVoice() {
        if (voice != -1) {
            return voice;
        }
        JSONObject muteStream = stream.muteStream;
        JSONObject remoteMuteStream = stream.remoteMuteStream;
        if (stream.isLocal) {
            if (muteStream != null) {
                voice = !muteStream.optBoolean("audio", false) ? 1 : 0;
            }
        } else {
            if (remoteMuteStream != null) {
                voice = !remoteMuteStream.optBoolean("audio", false) ? 1 : 0;
            }
        }
        return voice;
    }

    public void setVoice(int voice) {
        this.voice = voice;
    }

    public int getCamera() {
//        if (camera != -1) {
//            return camera;
//        }
        JSONObject muteStream = stream.muteStream;
        JSONObject remoteMuteStream = stream.remoteMuteStream;
        if (stream.isLocal) {
            if (muteStream != null) {
                camera = !muteStream.optBoolean("video", false) ? 1 : 0;
            }
        } else {
            if (remoteMuteStream != null) {
                camera = !remoteMuteStream.optBoolean("video", false) ? 1 : 0;
            }
        }
        return camera;
    }

    public static int getCamera(Stream stream) {
        int camera = 1;
        JSONObject muteStream = stream.muteStream;
        JSONObject remoteMuteStream = stream.remoteMuteStream;
        if (stream.isLocal) {
            if (muteStream != null) {
                camera = !muteStream.optBoolean("video", false) ? 1 : 0;
            }
        } else {
            if (remoteMuteStream != null) {
                camera = !remoteMuteStream.optBoolean("video", false) ? 1 : 0;
            }
        }
        return camera;
    }

    public void setCamera(int camera) {
        this.camera = camera;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof StreamData) {
            StreamData data = (StreamData) obj;
            if (data == null || data.getStream() == null || this.stream == null) {
                return false;
            }
            return TextUtils.equals(this.stream.streamId, data.getStream().streamId);
        }
        return false;
    }
}
