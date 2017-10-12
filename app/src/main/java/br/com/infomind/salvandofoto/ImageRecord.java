package br.com.infomind.salvandofoto;

import com.orm.SugarRecord;

/**
 * Created by Taniro on 08/10/2017.
 */

public class ImageRecord extends SugarRecord {

    private byte[] image;

    public ImageRecord() {
    }

    public ImageRecord(byte[] image) {
        this.image = image;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }
}
