package ku.piii.mp3;

import java.nio.file.Path;
import ku.piii.model.MusicMedia;

import com.mpatric.mp3agic.ID3v1;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MP3PathToMusicMapperImpl implements MP3PathToMusicMapper {

    private MusicMedia mapTag1(Mp3File mp3, MusicMedia m) {
        ID3v1 tag = mp3.getId3v1Tag();
        m.setTitle(tag.getTitle());
        m.setYear(tag.getYear());
        m.setGenre(tag.getGenreDescription());
        //Tag 1 style does not seem to have length

        return m;
    }

    private MusicMedia mapTag2(Mp3File mp3, MusicMedia m) {
        ID3v2 tag = mp3.getId3v2Tag();
        if (tag != null) {
            m.setTitle(tag.getTitle());
            m.setYear(tag.getYear());
            m.setLengthInSeconds(tag.getLength());
            m.setGenre(tag.getGenreDescription());
        }
        return m;

    }

    private enum Tag {
        T1, T2
    }

    @Override
    public MusicMedia mapPath(Path mp3File) {

        if (mp3File == null) {
            throw new IllegalArgumentException();
        }

        String path = mp3File.toString();
        MusicMedia m = new MusicMedia();
        m.setPath(path);

        try {

            Mp3File mp3 = new Mp3File(path);
            m = mp3.hasId3v1Tag()
                    ? mapTag1(mp3, m)
                    : mapTag2(mp3, m);

        } catch (IOException | UnsupportedTagException | InvalidDataException ex) {
            Logger.getLogger(MP3PathToMusicMapperImpl.class.getName()).log(Level.SEVERE, null, ex);
        }

        return m;
    }

}
