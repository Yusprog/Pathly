package bearmaps.proj2d.server.handler.impl;

import bearmaps.proj2d.AugmentedStreetMapGraph;
import bearmaps.proj2d.server.handler.APIRouteHandler;
import spark.Request;
import spark.Response;
import bearmaps.proj2d.utils.Constants;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

import static bearmaps.proj2d.utils.Constants.SEMANTIC_STREET_GRAPH;
import static bearmaps.proj2d.utils.Constants.ROUTE_LIST;

/**
 * Handles requests from the web browser for map images. These images
 * will be rastered into one large image to be displayed to the user.
 * @author rahul, Josh Hug, _________
 */
public class RasterAPIHandler extends APIRouteHandler<Map<String, Double>, Map<String, Object>> {

    /**
     * Each raster request to the server will have the following parameters
     * as keys in the params map accessible by,
     * i.e., params.get("ullat") inside RasterAPIHandler.processRequest(). <br>
     * ullat : upper left corner latitude, <br> ullon : upper left corner longitude, <br>
     * lrlat : lower right corner latitude,<br> lrlon : lower right corner longitude <br>
     * w : user viewport window width in pixels,<br> h : user viewport height in pixels.
     **/
    private static final String[] REQUIRED_RASTER_REQUEST_PARAMS = {"ullat", "ullon", "lrlat",
            "lrlon", "w", "h"};

    /**
     * The result of rastering must be a map containing all of the
     * fields listed in the comments for RasterAPIHandler.processRequest.
     **/
    private static final String[] REQUIRED_RASTER_RESULT_PARAMS = {"render_grid", "raster_ul_lon",
            "raster_ul_lat", "raster_lr_lon", "raster_lr_lat", "depth", "query_success"};


    @Override
    protected Map<String, Double> parseRequestParams(Request request) {
        return getRequestParams(request, REQUIRED_RASTER_REQUEST_PARAMS);
    }

    /**
     * Takes a user query and finds the grid of images that best matches the query. These
     * images will be combined into one big image (rastered) by the front end. <br>
     *
     *     The grid of images must obey the following properties, where image in the
     *     grid is referred to as a "tile".
     *     <ul>
     *         <li>The tiles collected must cover the most longitudinal distance per pixel
     *         (LonDPP) possible, while still covering less than or equal to the amount of
     *         longitudinal distance per pixel in the query box for the user viewport size. </li>
     *         <li>Contains all tiles that intersect the query bounding box that fulfill the
     *         above condition.</li>
     *         <li>The tiles must be arranged in-order to reconstruct the full image.</li>
     *     </ul>
     *
     * @param requestParams Map of the HTTP GET request's query parameters - the query box and
     *               the user viewport width and height.
     *
     * @param response : Not used by this function. You may ignore.
     * @return A map of results for the front end as specified: <br>
     * "render_grid"   : String[][], the files to display. <br>
     * "raster_ul_lon" : Number, the bounding upper left longitude of the rastered image. <br>
     * "raster_ul_lat" : Number, the bounding upper left latitude of the rastered image. <br>
     * "raster_lr_lon" : Number, the bounding lower right longitude of the rastered image. <br>
     * "raster_lr_lat" : Number, the bounding lower right latitude of the rastered image. <br>
     * "depth"         : Number, the depth of the nodes of the rastered image;
     *                    can also be interpreted as the length of the numbers in the image
     *                    string. <br>
     * "query_success" : Boolean, whether the query was able to successfully complete; don't
     *                    forget to set this to true on success! <br>
     */
    @Override
    public Map<String, Object> processRequest(Map<String, Double> requestParams, Response response) {
//        System.out.println("yo, wanna know the parameters given by the web browser? They are:");
//        System.out.println(requestParams);
        Map<String, Object> results = new HashMap<>();

        // "ullat", "ullon", "lrlat", "lrlon", "w", "h";
        double ullat = requestParams.get("ullat");
        double ullon = requestParams.get("ullon");
        double lrlat = requestParams.get("lrlat");
        double lrlon = requestParams.get("lrlon");
        double width = requestParams.get("w");

        //Corner Case 2: No Coverage,  ensure query_success is set to false.
        if (lrlon < Constants.ROOT_ULLON || Constants.ROOT_LRLON < ullon
                || lrlat > Constants.ROOT_ULLAT || ullat < Constants.ROOT_LRLAT
                || ullon >= lrlon || ullat <= lrlat) {
            results.put(REQUIRED_RASTER_RESULT_PARAMS[6], false);
            return results;
        }

        int depth = this.getDepth(lrlon, ullon, width);
        boolean success = true;
        String [][] img;

        //System.out.println("Since you haven't implemented RasterAPIHandler.processRequest, nothing is displayed in "
        //        + "your browser." + " THIS IS THE DEPTH " + depth);

        List<Integer> lons = getX(lrlon, ullon, depth, results);
        List<Integer> lats = getY(lrlat, ullat, depth, results);

//        System.out.println("lons length " + lons.size() + " lats length " + lats.size());
//
//        for (int i = 0; i < lats.size(); i++) {
//            System.out.println("lats " + i + ": " + lats.get(i));
//        }
//
//        for (int j = 0; j < lons.size(); j++) {
//            System.out.println("lons " + j + ": " + lons.get(j));
//        }


        if (depth == 0) {
            img = new String[1][1];
            img [0][0]= "d0_x0_y0.png";
        } else {
            img = getImages(lons, lats, depth);
        }


        results.put("render_grid", img);
        results.put("depth", depth);
        results.put("query_success", success);

        return results;
    }

    private int getDepth(double lrlon, double ullon, double width) {
        double LonDPP = (lrlon - ullon) / width;
        int depth;

        if (LonDPP >= 0.000343322753906){
            depth = 0;
        } else if (LonDPP >= 0.000171661376) {
            depth = 1;
        } else if (LonDPP >= 0.00008583068847) {
            depth = 2;
        } else if (LonDPP >= 0.000042915344) {
            depth = 3;
        } else if (LonDPP >= 0.000021457672) {
            depth = 4;
        } else if (LonDPP >= 0.00001072883606) {
            depth = 5;
        } else if (LonDPP >= 0.00000536441803) {
            depth = 6;
        } else {
            depth = 7;
        }

        return depth;
    }

    private List<Integer> getX(double lrlon, double ullon, int depth, Map<String, Object> results) {
        List<Integer> lons = new ArrayList<>();

        double bucketRange = (Constants.ROOT_LRLON - Constants.ROOT_ULLON) / Math.pow(2, depth);
        //System.out.println("bRange: " + bucketRange);

        int xLeft = (int) Math.abs((Constants.ROOT_ULLON - ullon)/bucketRange);
        //System.out.println("xL: " + xLeft);
        double bound_ullon = Constants.ROOT_ULLON + (xLeft * bucketRange);
        //System.out.println("bound ullon: " + bound_ullon);
        results.put("raster_ul_lon", bound_ullon);


        int xRight = (int) Math.abs(Math.ceil((Constants.ROOT_ULLON - lrlon)/bucketRange));
        //System.out.println("xR: " + xRight);
        double bound_lrlon = Constants.ROOT_ULLON + ((xRight+1) * bucketRange);
        //System.out.println("bound lrlon: " + bound_lrlon);
        results.put("raster_lr_lon", bound_lrlon);

        for (int i = xLeft; i <= xRight; i++) {
            lons.add(i);
        }

        return lons;
    }

    private List<Integer> getY(double lrlat, double ullat, int depth, Map<String, Object> results) {
        List<Integer> lats = new ArrayList<>();

        double bucketRange = (Constants.ROOT_ULLAT - Constants.ROOT_LRLAT) / Math.pow(2, depth);
        //System.out.println("bRange: " + bucketRange);

        int xUp = (int) Math.abs((Constants.ROOT_ULLAT - ullat)/bucketRange);
        //System.out.println("xup: " + xUp);
        double bound_ullat = Constants.ROOT_ULLAT - (xUp * bucketRange);
        //System.out.println("bound ullat: " + bound_ullat);
        results.put("raster_ul_lat", bound_ullat);

        int xDown = (int) Math.abs(Math.ceil((Constants.ROOT_ULLAT - lrlat)/bucketRange));
        //System.out.println("xdown: " + xDown);
        double bound_lrlat = Constants.ROOT_ULLAT - (xDown * bucketRange);
        //System.out.println("bound lrlat: " + bound_lrlat);
        results.put("raster_lr_lat", bound_lrlat);

        for (int i = xUp; i < xDown; i++) {
            lats.add(i);
        }

        return lats;
    }

    public String[][] getImages(List<Integer> lons, List<Integer> lats, int depth) {
        String strDepth = Integer.toString(depth);
        String[][] images = new String[lats.size()][lons.size()];

        for (int i = 0; i < lats.size(); i++) {
            for (int j = 0; j < lons.size(); j++) {
                String lon = Integer.toString(lons.get(j));
                String lat = Integer.toString(lats.get(i));
                images[i][j] = "d" + strDepth + "_x" + lon + "_y" + lat + ".png";
            }
        }

        return images;
    }

//    @Override
//    public Map<String, Object> processRequest(Map<String, Double> requestParams, Response response) {
////        System.out.println("yo, wanna know the parameters given by the web browser? They are:");
////        System.out.println(requestParams);
//        Map<String, Object> results = new HashMap<>();
////        System.out.println("Since you haven't implemented RasterAPIHandler.processRequest, nothing is displayed in "
////                + "your browser.");
//        double lrlon = requestParams.get("lrlon");
//        double ullon = requestParams.get("ullon");
//        double width = requestParams.get("w");
//        double ullat = requestParams.get("ullat");
//        double lrlat = requestParams.get("lrlat");
//
//        if (Constants.ROOT_LRLON < ullon || Constants.ROOT_LRLAT > ullat
//                || Constants.ROOT_ULLON > lrlon || Constants.ROOT_ULLAT < lrlat
//                || lrlon < ullon || ullat < lrlat) {
//            results.put("query_success", false);
//            return results;
//        }
//
//        int depth = getImageDepth(ullon, lrlon, width);
//
//        double lonDistPerTile = (Constants.ROOT_LRLON - Constants.ROOT_ULLON) / Math.pow(2, depth);
//        double latDistPerTile = (Constants.ROOT_ULLAT - Constants.ROOT_LRLAT) / Math.pow(2, depth);
//        int ulX = (int) Math.abs((ullon - Constants.ROOT_ULLON) / lonDistPerTile);
//        int ulY = (int) Math.abs((ullat - Constants.ROOT_ULLAT) / latDistPerTile);
//        int lrX = (int) Math.abs((lrlon - Constants.ROOT_ULLON) / lonDistPerTile);
//        int lrY = (int) Math.abs((lrlat - Constants.ROOT_ULLAT) / latDistPerTile);
//
//        String[][] renderGrid = new String[lrY - ulY + 1][lrX - ulX + 1];
//        for (int i = 0; i <= lrY - ulY; i += 1) {
//            for (int j = 0; j <= lrX - ulX; j += 1) {
//                renderGrid[i][j] = "d" + depth + "_x" + (ulX + j) + "_y" + (ulY + i) + ".png";
//            }
//        }
//
//        double rasterUllon = Constants.ROOT_ULLON + ulX * lonDistPerTile;
//        double rasterUllat = Constants.ROOT_ULLAT - ulY * latDistPerTile;
//        double rasterLrlon = Constants.ROOT_ULLON + (lrX + 1) * lonDistPerTile;
//        double rasterLrlat = Constants.ROOT_ULLAT - (lrY + 1) * latDistPerTile;
//
//        results.put("raster_ul_lon", rasterUllon);
//        results.put("raster_ul_lat", rasterUllat);
//        results.put("raster_lr_lon", rasterLrlon);
//        results.put("raster_lr_lat", rasterLrlat);
//        results.put("render_grid", renderGrid);
//        results.put("depth", depth);
//        results.put("query_success", true);
//
//        return results;
//    }
//
//    private int getImageDepth(double ullon, double lrlon, double width) {
//        double maxLonDPP = lonDPP(lrlon, ullon, width);
//        int depth = 0;
//        double lonDPP = (Constants.ROOT_LRLON - Constants.ROOT_ULLON) / Constants.TILE_SIZE;
//        while (lonDPP > maxLonDPP && depth < 7) {
//            depth += 1;
//            lonDPP /= 2;
//        }
//        return depth;
//    }
//
//    private double lonDPP(double lrlon, double ullon, double width) {
//        return (lrlon - ullon) / width;
//    }



    @Override
    protected Object buildJsonResponse(Map<String, Object> result) {
        boolean rasterSuccess = validateRasteredImgParams(result);

        if (rasterSuccess) {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            writeImagesToOutputStream(result, os);
            String encodedImage = Base64.getEncoder().encodeToString(os.toByteArray());
            result.put("b64_encoded_image_data", encodedImage);
        }
        return super.buildJsonResponse(result);
    }

    private Map<String, Object> queryFail() {
        Map<String, Object> results = new HashMap<>();
        results.put("render_grid", null);
        results.put("raster_ul_lon", 0);
        results.put("raster_ul_lat", 0);
        results.put("raster_lr_lon", 0);
        results.put("raster_lr_lat", 0);
        results.put("depth", 0);
        results.put("query_success", false);
        return results;
    }

    /**
     * Validates that Rasterer has returned a result that can be rendered.
     * @param rip : Parameters provided by the rasterer
     */
    private boolean validateRasteredImgParams(Map<String, Object> rip) {
        for (String p : REQUIRED_RASTER_RESULT_PARAMS) {
            if (!rip.containsKey(p)) {
                System.out.println("Your rastering result is missing the " + p + " field.");
                return false;
            }
        }
        if (rip.containsKey("query_success")) {
            boolean success = (boolean) rip.get("query_success");
            if (!success) {
                System.out.println("query_success was reported as a failure");
                return false;
            }
        }
        return true;
    }

    /**
     * Writes the images corresponding to rasteredImgParams to the output stream.
     * In Spring 2016, students had to do this on their own, but in 2017,
     * we made this into provided code since it was just a bit too low level.
     */
    private  void writeImagesToOutputStream(Map<String, Object> rasteredImageParams,
                                                  ByteArrayOutputStream os) {
        String[][] renderGrid = (String[][]) rasteredImageParams.get("render_grid");
        int numVertTiles = renderGrid.length;
        int numHorizTiles = renderGrid[0].length;

        BufferedImage img = new BufferedImage(numHorizTiles * Constants.TILE_SIZE,
                numVertTiles * Constants.TILE_SIZE, BufferedImage.TYPE_INT_RGB);
        Graphics graphic = img.getGraphics();
        int x = 0, y = 0;

        for (int r = 0; r < numVertTiles; r += 1) {
            for (int c = 0; c < numHorizTiles; c += 1) {
                graphic.drawImage(getImage(Constants.IMG_ROOT + renderGrid[r][c]), x, y, null);
                x += Constants.TILE_SIZE;
                if (x >= img.getWidth()) {
                    x = 0;
                    y += Constants.TILE_SIZE;
                }
            }
        }

        /* If there is a route, draw it. */
        double ullon = (double) rasteredImageParams.get("raster_ul_lon"); //tiles.get(0).ulp;
        double ullat = (double) rasteredImageParams.get("raster_ul_lat"); //tiles.get(0).ulp;
        double lrlon = (double) rasteredImageParams.get("raster_lr_lon"); //tiles.get(0).ulp;
        double lrlat = (double) rasteredImageParams.get("raster_lr_lat"); //tiles.get(0).ulp;

        final double wdpp = (lrlon - ullon) / img.getWidth();
        final double hdpp = (ullat - lrlat) / img.getHeight();
        AugmentedStreetMapGraph graph = SEMANTIC_STREET_GRAPH;
        List<Long> route = ROUTE_LIST;

        if (route != null && !route.isEmpty()) {
            Graphics2D g2d = (Graphics2D) graphic;
            g2d.setColor(Constants.ROUTE_STROKE_COLOR);
            g2d.setStroke(new BasicStroke(Constants.ROUTE_STROKE_WIDTH_PX,
                    BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            route.stream().reduce((v, w) -> {
                g2d.drawLine((int) ((graph.lon(v) - ullon) * (1 / wdpp)),
                        (int) ((ullat - graph.lat(v)) * (1 / hdpp)),
                        (int) ((graph.lon(w) - ullon) * (1 / wdpp)),
                        (int) ((ullat - graph.lat(w)) * (1 / hdpp)));
                return w;
            });
        }

        rasteredImageParams.put("raster_width", img.getWidth());
        rasteredImageParams.put("raster_height", img.getHeight());

        try {
            ImageIO.write(img, "png", os);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private BufferedImage getImage(String imgPath) {
        BufferedImage tileImg = null;
        if (tileImg == null) {
            try {
//                File in = new File(imgPath);
                tileImg = ImageIO.read(Thread.currentThread().getContextClassLoader().getResource(imgPath));
            } catch (IOException | NullPointerException e) {
                e.printStackTrace();
            }
        }
        return tileImg;
    }
}
