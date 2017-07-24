package com.example.android.camera2basic;

import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Created by Yoshiki on 2017/07/15.
 */

public class Product {
    public String imageURL; //商品画像のURL
    public Review[] reviews; //レビュー(タイトルと本文)を複数格納する配列
    public Float rating; //評価点数
    public Integer price; //商品価格
    public String name; //商品名
    public Boolean favorite = false; // お気に入りフラグ
    public String amazonURL;

    public static String iFrameUrl;
    public static String iFrameHTML; // レビューや評価点数の書いてあるHTMLファイル。 ASyncを利用する為に事前に定義しておく。

    public Product(String _name, Integer _price, Review[] _reviews, Float _rating, String _imageURL, String _amazonURL)
    {
        name = _name;
        price = _price;
        reviews = _reviews;
        rating = _rating;
        imageURL = _imageURL;
        amazonURL = _amazonURL;

        setStaticVar();
    }

    // Amazon APIから帰ってきた情報を使ってデータを格納する場合
    public Product(String _amazon_xml)
    {
        System.out.println("Product");
        reviews = new Review[3];
        getProductForXML(_amazon_xml);

        setStaticVar();
    }

    public Product()
    {
        name = "";
        price = 0;
        reviews = new Review[3];
        reviews[0] = new Review("","");
        reviews[1] = new Review("","");
        reviews[2] = new Review("","");
        rating = 0f;
        imageURL = "https://images-na.ssl-images-amazon.com/images/I/51c061aHw4L.jpg";
        amazonURL = "";

        setStaticVar();
    }

    public void setStaticVar()
    {
        iFrameUrl ="";
        iFrameHTML = "";

    }


    public void setDammyData(){
        name = "知識ゼロから学ぶソフトウェアテスト 【改訂版】";
        price = 2592;
        imageURL = "https://images-na.ssl-images-amazon.com/images/I/51c061aHw4L.jpg";
        reviews[0] = new Review("テストの基本", "　プログラマー出身の管理職ですが、テストの基本的な技術についても勉強してみたいと思い、本書の評判を聞き読んでみました。\n" +
                "　前々からテストについて思っていたのは、プログラミングに比べ、全然技術が発展しないと感じていました。実際のところ難しい分野だと再認識しました。\n" +
                "本書を読んでみて知らないこともたくさんあり、勘違いしていることもありました。\n" +
                "一番知らなくて恥ずかしく思ったのがIEEE829の存在です。テンプレートがあるようなので、これも読んでみたいと思いました。\n" +
                "アジャイル開発に置けるテストとはあまり関係なく、テストの基本が分かりやすく書いてある１冊です。\n" +
                "　テスターであるなら絶対に読んだ方がよい一冊ですし、プログラマーも１度は読んでおきたいと思いました。");
        reviews[1] = new Review("簡潔で明瞭", "テストについて簡潔で分かりやすく語られている。自分の何となく必要だろうと経験的に感じていたこととそれほど乖離は無かったので安心出来た。IEEE829という標準を初めて存在を知った。\n" +
                "本書を読んでいると、無理難題を押し付けられるのは、MS社であっても同じなんだなぁと諦めを感じつつも、戦うべきことはどこの現場にもあるのだと、覚悟を決めるしかないんだなとも思った。\n" +
                "探索テストはイマイチ分からなかったのでもう一度読み込もう。\n" +
                "もし新人がテストについて尋ねてきたらこの本を読ませよう。そして、IEEE829のテンプレートをまずは満たしてテスト仕様書を用意してくれと言おう。それが全てではないが、テストの入り口としては本書とIEEE829は相当の武器となりうるはすだ。");
        reviews[2] = new Review("説明が雑では", "言っていることがちらほら矛盾していたり、中途半端に用語を紹介するだけで結局その概念の本質を説明できていなかったり…テストの本なのに論理性を欠いていてストレスを感じます。「厳密で難解な説明」を避けているのはわかるのですが、「本質を平易に説明する」のではなく「非本質的な事を雑に述べている」という印象を受けました。ただ「（非本質的かもしれないけれども）経験的には大事なこと」が多く述べられているのは良い点であると思います（「知識ゼロ」からの人が求める話題かどうかは疑問）。\n" +
                "\n" +
                "おおまかな概念・用語や雰囲気をつかむには多分よいのだろうと思いますし、俯瞰的な観点や大事な知見など得られるところも多々あると思うのですが、自分には気になるところが多かったです。");
        rating = 4.5f;
        amazonURL = "https://www.amazon.co.jp/dp/B00HQ7S5CA/ref=dp-kindle-redirect?_encoding=UTF8&btkr=1";

    }

    public void getProductForXML(String amazon_xml)
    {
        try{

            // 取得できているかを確認
            if(amazon_xml.length() > 0) {
                //System.out.println(amazon_xml);
                InputSource inputSource = new InputSource(new StringReader(amazon_xml));

                //DOMを使うためのインスタンス取得
                Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputSource);

                Element root = document.getDocumentElement();

                // Itemノード(一番上の商品)を取得
                Node items = root.getLastChild();

                if(checkErrorNode(items)) {
                    Node item = getChildNodeByNodeName(items, "Item");
                    Node itemAttributes = getChildNodeByNodeName(item, "ItemAttributes");
                    Node mediumImage = getChildNodeByNodeName(getChildNodeByNodeName(item, "ImageSets").getFirstChild(), "MediumImage");

                    if (!name.equals(getChildNodeByNodeName(itemAttributes, "Title").getTextContent())) {
                        name = getChildNodeByNodeName(itemAttributes, "Title").getTextContent();
                        price = Integer.parseInt(getChildNodeByNodeName(item, "OfferSummary").getFirstChild().getFirstChild().getTextContent());
                        imageURL = getChildNodeByNodeName(mediumImage, "URL").getTextContent();
                        amazonURL = getChildNodeByNodeName(item, "DetailPageURL").getTextContent();
                    }

                    String nextIFrameURL = getChildNodeByNodeName(item, "CustomerReviews").getFirstChild().getTextContent();

                    if (!iFrameUrl.equals(nextIFrameURL) || reviews[0].title.equals("")) {
                        // iFrameUrl = レビューや評価点数が書かれたHTMLのURLを取得
                        iFrameUrl = nextIFrameURL;
                        // iFrameHTMLにレビューや評価点数が書かれたHTMLを格納する。
                        getHTML(iFrameUrl);
                        // GetHTMLの中でsetDataByHtml()を呼び出して評価やレビューを取得している
                    }
                }
            }
        }
        catch(SAXException e){
            System.out.println("SAXException");
        }
        catch(IOException e)
        {
            System.out.println("IOException");
        }
        catch(ParserConfigurationException e)
        {
            System.out.println("ParserConfigurationException");
        }
    }

    public Boolean checkErrorNode(Node node)
    {
        Node errors = getChildNodeByNodeName(getChildNodeByNodeName(node,"Request"), "Errors");
        if(errors == null) {
            return true;
        }
        return false;
    }


    // parentの子ノードの中から、nodenameと等しいノード名を持つノードを返却する
    public Node getChildNodeByNodeName(Node parent, String nodename)
    {
        if(parent == null)
            return null;
        NodeList childs = parent.getChildNodes();
        int i = 0;
        while(i < childs.getLength())
        {
            if(childs.item(i).getNodeName().equals(nodename))
            {
                // System.out.println("HIT");
                return childs.item(i);
            }
            i++;
        }
        return null;
    }

    // iFrameURLに対応したHTMLファイルを取得し、このクラスのiFrameHTMLに格納する。
    public synchronized void getHTML(String iFrameURL)
    {
        GetHTML task = new GetHTML(this);
        task.execute(iFrameURL);

    }

    // 非同期処理通信後に行われる、HTMLからデータを読み取り更新する関数
    public void setDataByHtml()
    {
        getRatingByHtml();
        getReviewsByHtml();
    }

    public void getRatingByHtml()
    {
        // 過剰なアクセスなどでAmazonに繋がらなくなった場合、エラーで実行出来なくなる。
        try {
            if (iFrameHTML != null) {
                // org.w3c.dom.Document と重複する為、org~から記述
                org.jsoup.nodes.Document doc = Jsoup.parse(iFrameHTML);
                //数値が0の場合は、 histoRowfive と div.histoCount の間にaが入らない
                Integer rating_five = Integer.parseInt(doc.select("div.histoRowfive div.histoCount").text());
                Integer rating_four = Integer.parseInt(doc.select("div.histoRowfour div.histoCount").text());
                Integer rating_three = Integer.parseInt(doc.select("div.histoRowthree div.histoCount").text());
                Integer rating_two = Integer.parseInt(doc.select("div.histoRowtwo div.histoCount").text());
                Integer rating_one = Integer.parseInt(doc.select("div.histoRowone div.histoCount").text());
                Integer sum = (rating_five * 5) + (rating_four * 4) + (rating_three * 3) + (rating_two * 2) + (rating_one);
                Integer count = rating_five + rating_four + rating_three + rating_two + rating_one;
                rating = (Float) sum.floatValue()/count.floatValue();
            }
        }
        catch(Exception e)
        {
            System.out.println("Rating : Exception");
            rating = 0f;
        }
    }

    public void getReviewsByHtml()
    {
//        System.out.println("レビュー取得");
//        System.out.println(html);
        String title = "";
        String text = "";
        // 過剰なアクセスなどでAmazonに繋がらなくなった場合、エラーで実行出来なくなる。
        try {
            if (iFrameHTML != null) {
                org.jsoup.nodes.Document doc = Jsoup.parse(iFrameHTML);
                // レビューが書かれているテーブルを取得

                Elements review_1 = doc.select("body > div.crIFrame > div.crIframeReviewList > table > tbody > tr > td > div:nth-child(3)");
                if(review_1 != null)
                {
                    title = doc.select("body > div.crIFrame > div.crIframeReviewList > table > tbody > tr > td > div:nth-child(3) > div > b").text();
                    text = doc.select("body > div.crIFrame > div.crIframeReviewList > table > tbody > tr > td > div:nth-child(3) > div.reviewText").text();
                    reviews[0].title = title;
                    reviews[0].text = text;
                    System.out.println(title);
                }
                Elements review_2 = doc.select("body > div.crIFrame > div.crIframeReviewList > table > tbody > tr > td > div:nth-child(6)");
                if(review_1 != null)
                {
                    title = doc.select("body > div.crIFrame > div.crIframeReviewList > table > tbody > tr > td > div:nth-child(6) > div > b").text();
                    text = doc.select("body > div.crIFrame > div.crIframeReviewList > table > tbody > tr > td > div:nth-child(6) > div.reviewText").text();
                    reviews[1].title = title;
                    reviews[1].text = text;
                }
                Elements review_3 = doc.select("body > div.crIFrame > div.crIframeReviewList > table > tbody > tr > td > div:nth-child(9)");
                if(review_1 != null)
                {
                    title = doc.select("body > div.crIFrame > div.crIframeReviewList > table > tbody > tr > td > div:nth-child(9) > div > b").text();
                    text = doc.select("body > div.crIFrame > div.crIframeReviewList > table > tbody > tr > td > div:nth-child(9) > div.reviewText").text();
                    reviews[2].title = title;
                    reviews[2].text = text;
                }
            }
        }
        catch(Exception e)
        {
            reviews[0] = new Review("", "");
            reviews[1] = new Review("", "");
            reviews[2] = new Review("", "");
        }
    }


}
