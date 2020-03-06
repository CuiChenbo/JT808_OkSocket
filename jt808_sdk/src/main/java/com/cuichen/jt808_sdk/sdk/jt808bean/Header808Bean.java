package com.cuichen.jt808_sdk.sdk.jt808bean;

public class Header808Bean {

    /**
     * mobile : 15651821852
     * msgID : 2
     * bodyAttr : {"split":false,"encrypt":false,"bodyLength":0}
     * seqNO : 1
     */

    private String mobile;
    private int msgID;
    private BodyAttrBean bodyAttr;
    private int seqNO;

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public int getMsgID() {
        return msgID;
    }

    public void setMsgID(int msgID) {
        this.msgID = msgID;
    }

    public BodyAttrBean getBodyAttr() {
        return bodyAttr;
    }

    public void setBodyAttr(BodyAttrBean bodyAttr) {
        this.bodyAttr = bodyAttr;
    }

    public int getSeqNO() {
        return seqNO;
    }

    public void setSeqNO(int seqNO) {
        this.seqNO = seqNO;
    }

    public static class BodyAttrBean {
        /**
         * split : false
         * encrypt : false
         * bodyLength : 0
         */

        private boolean split;
        private boolean encrypt;
        private int bodyLength;

        public boolean isSplit() {
            return split;
        }

        public void setSplit(boolean split) {
            this.split = split;
        }

        public boolean isEncrypt() {
            return encrypt;
        }

        public void setEncrypt(boolean encrypt) {
            this.encrypt = encrypt;
        }

        public int getBodyLength() {
            return bodyLength;
        }

        public void setBodyLength(int bodyLength) {
            this.bodyLength = bodyLength;
        }

        @Override
        public String toString() {
            return "BodyAttrBean{" +
                    "split=" + split +
                    ", encrypt=" + encrypt +
                    ", bodyLength=" + bodyLength +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "Header808Bean{" +
                "mobile='" + mobile + '\'' +
                ", msgID=" + msgID +
                ", bodyAttr=" + bodyAttr.toString() +
                ", seqNO=" + seqNO +
                '}';
    }
}
