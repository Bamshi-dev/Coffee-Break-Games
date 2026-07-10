package seda_project.control_alt_defeat.gamebox.HexChess.Engine;

public class HexCoord {

    public final int row;
    public final int col;

    private static final int[] COL_SIZES = {6,7,8,9,10,11,10,9,8,7,6};

    public HexCoord(int col, int row){
        this.row = row;
        this.col = col;
    }

    /**
     * Transform a String into a HexCoord
     * @param id
     * @return
     * update: validate if the string is not empty
     */
    public static HexCoord transformIdToHex(String id){
        if (id == null || id.length() < 2) throw new IllegalArgumentException("Invalid ID format");
        char colChar = id.charAt(0);
        int col = colChar - 'a';
        String rowStr = id.substring(1);
        int row = Integer.parseInt(rowStr);

        return new HexCoord(col, row);
    }
    /**
     * Retuns the Id of a HexCoord
     * @return
     */
    public String transformHextoId(){
        return String.valueOf((char)(col +'a'))+row;
    }

    /**
     * Checks wether this Coord is a valid one
     * @param col
     * @param row
     * @return
     */
    public boolean isValid(int col, int row){
        if (col < 0 || col > 10) {
            return false;
        }
        boolean rowvalid = (row >= 1 && row <= COL_SIZES[col]);
        return rowvalid;
    }

}
