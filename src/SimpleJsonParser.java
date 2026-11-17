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

        int braceDepth = 0;
        int closeBraceIndex = -1;
        boolean insideDoc = false;
        for(int i = docIndex; i < json.length(); i++){
            char c = json.charAt(i);
            if(c == '{'){
                if(!insideDoc) insideDoc = true;
                braceDepth++;
            } else if (c == '}') {
                braceDepth--;
                if (insideDoc && braceDepth == 0){
                    closeBraceIndex = i;
                    break;
                }
            }
        }

        if(closeBraceIndex == -1) closeBraceIndex = json.length();

        int searchStartIndex = docIndex;
        if (fieldName.equals("file_id")){
            int thumbIndex = json.indexOf("\"thumb\"", docIndex);
            if(thumbIndex != -1 && thumbIndex < closeBraceIndex){
                int thumbDepth = 0;
                int thumbClose = -1;
                for (int i = thumbIndex; i < json.length(); i++){
                    char c = json.charAt(i);
                    if(c == '{'){
                        thumbDepth++;
                    } else if (c == '}') {
                        thumbDepth--;
                        if(thumbDepth == 0){
                            thumbClose = i;
                            break;
                        }
                    }
                }
                if(thumbClose != -1){
                    searchStartIndex = thumbClose;
                }
            }
        }


        String searchKey = "\"" + fieldName + "\"";
        int index = json.indexOf(searchKey, searchStartIndex);
        if(index == -1 || index > closeBraceIndex) return null;

        int colonIndex = json.indexOf(':' , index + searchKey.length());
        if (colonIndex == -1) return null;

       // int quoteIndex = json.indexOf(':', colonIndex);

        int valStartQuote = json.indexOf('"', colonIndex);
        if (valStartQuote == -1) return null;

        int startIndex = valStartQuote + 1;
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
