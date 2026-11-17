public class SimpleJsonParser {

    // Helper method to extract a long value given a key (like chat id)
    public static long extractLong(String json, String key){

        String searchKey = "\"" + key + "\"";
        int index = json.indexOf(searchKey);
        if(index == -1) return -1;

        int colonIndex = json.indexOf(':' , index + searchKey.length());
        if(colonIndex == -1) return -1;

        int startIndex = colonIndex + 1;
        while (startIndex < json.length() && Character.isWhitespace(json.charAt(startIndex))){
            startIndex++;
        }

        int endIndex = startIndex;
        // Read character until we hit a comma, closing brace, or whitespace
        while(endIndex < json.length()){

            char c = json.charAt(endIndex);

            if( c == ',' || c == '}' || c == ']' || Character.isWhitespace(c)){
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
        String searchKey = "\"" + key + "\"";
        int index = json.indexOf(searchKey);
        if(index == -1) return null;

        int colonIndex = json.indexOf(':' , index + searchKey.length());
        if(colonIndex == -1) return null;

        int quoteIndex = json.indexOf('"' , colonIndex + 1);
        if(quoteIndex == -1) return null;

        int startIndex = quoteIndex + 1;
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

    public static String extractDocumentField(String json, String fieldName){

        int docIndex = json.indexOf("\"document\"");
        if(docIndex == -1) return null;

        String searchKey = "\"" + fieldName + "\"";
        int index = json.indexOf(searchKey, docIndex);
        if(index == -1) return null;

        int colonIndex = json.indexOf(':' , index + searchKey.length());
        if (colonIndex == -1) return null;

        int quoteIndex = json.indexOf('"', colonIndex + 1);
        if (quoteIndex == -1) return null;

        int startIndex = quoteIndex + 1;
        int endIndex = startIndex;

        while (endIndex < json.length()){
            if(json.charAt(endIndex) =='"' && json.charAt(endIndex - 1) != '\\' ){
                break;
            }
            endIndex++;
        }

        if(endIndex >= json.length()) return null;
        return json.substring(startIndex, endIndex);
    }
}
