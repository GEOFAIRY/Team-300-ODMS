package seng302.User.Importers;

import com.google.gson.reflect.TypeToken;
import seng302.User.User;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class UserReaderJSON implements ProfileReader<User> {

    /**
     * gets user profiles from a json file
     * @param path the path to the json file
     * @return returns the imported profiles
     */
    public List<User> getProfiles(String path) {
        JSONParser<User> parser = new JSONParser<>(new TypeToken<ArrayList<User>>() {}.getType());
        Path filePath = parser.checkPath(path);
        if (filePath == null) {
            return null;
        }
        return parser.readJson(filePath);
    }
}
