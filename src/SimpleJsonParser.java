public class SimpleJsonParser {

    // Helper method to extract a long value given a key (like chat id)
    public static long extractLong(String json, String key){

        String searchKey = "\"" + key + "\":";
        int index = json.indexOf(searchKey);
        if(index == -1) return -1;

        int startIndex = index + searchKey.length();
        int endIndex = startIndex;

        // Read character until we hit a comma, closing brace, or whitespace
        while(endIndex < json.length()){
            char c = json.charAt(endIndex);
            if( c == ',' || c == '}' || c == ' ' || c == '\n' || c == '\r'){
                break;
            }
            endIndex++;
        }

        try {
            String valuesStr = json.substring(startIndex, endIndex).trim();
            return Long.parseLong(valuesStr);
        }catch (NumberFormatException e){
            return -1;
        }
    }

    // Helper method to extract string value (like msg text) given a key
    public static String extractString(String json, String key){
        String searchKey = "\"" + key + "\":\"";
        int index = json.indexOf(searchKey);
        if(index == -1) return null;

        int startIndex = index + searchKey.length();
        int endIndex = startIndex;

        while (endIndex < json.length()){
            if(json.charAt(endIndex) == '"' && json.charAt(endIndex - 1) != '\\'){
                break;
            }
            endIndex++;
        }
        if(endIndex >= json.length()) return null;
        return json.substring(startIndex, endIndex);
    }
}
