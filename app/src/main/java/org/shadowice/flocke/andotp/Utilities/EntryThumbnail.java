/*
 * Copyright (C) 2017-2020 Jakob Nixdorf
 * Copyright (C) 2017-2020 Richy HBM
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.shadowice.flocke.andotp.Utilities;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.content.res.AppCompatResources;

import org.shadowice.flocke.andotp.R;

public class EntryThumbnail {
    private enum AssetType {
        Bitmap,
        Vector
    }

    public enum EntryThumbnails {
        Default(R.mipmap.ic_launcher_round),
        OneAndOne(R.drawable.thumb_1and1),
        OnePassword(R.drawable.thumb_1password),
        Adafruit(R.drawable.thumb_adafruit),
        AdGuard(R.drawable.thumb_adguard),
        Adobe(R.drawable.thumb_adobe),
        AirBNB(R.drawable.thumb_airbnb),
        Airbrake(R.drawable.thumb_airbrake),
        AirTable(R.drawable.thumb_airtable),
        AllegroPl(R.drawable.thumb_allegropl),
        Amazon(R.drawable.thumb_amazon),
        AmazonWebServices(R.drawable.thumb_amazonwebservices),
        AngelList(R.drawable.thumb_angellist),
        AnimeBytes(R.drawable.thumb_animebytes),
        Anonaddy(R.drawable.thumb_anonaddy),
        Apache(R.drawable.thumb_apache),
        Apple(R.drawable.thumb_apple),
        Appveyor(R.drawable.thumb_appveyor),
        ArenaNet(R.drawable.thumb_arenanet),
        AtlanticNet(R.drawable.thumb_atlantic_net),
        Atlassian(R.drawable.thumb_atlassian),
        AVM(R.drawable.thumb_avm),
        Backblaze(R.drawable.thumb_backblaze),
        BattleNet(R.drawable.thumb_battlenet),
        Betterment(R.drawable.thumb_betterment),
        Binance(R.drawable.thumb_binance),
        BitBucket(R.drawable.thumb_bitbucket),
        Bitcoin(R.drawable.thumb_bitcoin),
        Bitdefender(R.drawable.thumb_bitdefender),
        Bitfinex(R.drawable.thumb_bitfinex),
        Bitmex(R.drawable.thumb_bitmex),
        Bitpanda(R.drawable.thumb_bitpanda),
        Bitstamp(R.drawable.thumb_bitstamp),
        Bittrex(R.drawable.thumb_bittrex),
        Bitwala(R.drawable.thumb_bitwala),
        Bitwarden(R.drawable.thumb_bitwarden),
        BlockchainInfo(R.drawable.thumb_blockchain_info),
        Bugcrowd(R.drawable.thumb_bugcrowd),
        CEXio(R.drawable.thumb_cexio),
        ChurchTools(R.drawable.thumb_church_tools),
        Cisco(R.drawable.thumb_cisco),
        CloudDownload(R.drawable.thumb_cloud_download),
        Cloudflare(R.drawable.thumb_cloudflare),
        Cobranded(R.drawable.thumb_cobranded),
        Codegiant(R.drawable.thumb_codegiant),
        Coinbase(R.drawable.thumb_coinbase),
        CoinJar(R.drawable.thumb_coinjar),
        ComputerBase(R.drawable.thumb_computerbase),
        ConnectWiseManage(R.drawable.thumb_connectwise_manage),
        CozyCloud(R.drawable.thumb_cozycloud),
        Crowdin(R.drawable.thumb_crowdin),
        Dashlane(R.drawable.thumb_dashlane),
        Debian(R.drawable.thumb_debian),
        Degiro(R.drawable.thumb_degiro),
        Denic(R.drawable.thumb_denic),
        Diaspora(R.drawable.thumb_diaspora),
        Digidentity(R.drawable.thumb_digidentity),
        DigitalOcean(R.drawable.thumb_digital_ocean),
        Discord(R.drawable.thumb_discord),
        Discourse(R.drawable.thumb_discourse),
        Disroot(R.drawable.thumb_disroot),
        Docker(R.drawable.thumb_docker),
        DocuSign(R.drawable.thumb_docusign),
        DreamHost(R.drawable.thumb_dreamhost),
        Dropbox(R.drawable.thumb_dropbox),
        Drupal(R.drawable.thumb_drupal),
        ElectronicArts(R.drawable.thumb_electronic_arts),
        Email(R.drawable.thumb_email),
        EpicGames(R.drawable.thumb_epic_games),
        Etsy(R.drawable.thumb_etsy),
        Eveonline(R.drawable.thumb_eveonline),
        Evernote(R.drawable.thumb_evernote),
        Facebook(R.drawable.thumb_facebook),
        FACEIT(R.drawable.thumb_faceit),
        Fanatical(R.drawable.thumb_fanatical),
        Fastmail(R.drawable.thumb_fastmail),
        Figma(R.drawable.thumb_figma),
        Fingerprint(R.drawable.thumb_fingerprint),
        Finnair(R.drawable.thumb_finnair),
        Firefox(R.drawable.thumb_firefox),
        Flight(R.drawable.thumb_flight_takeoff),
        Floatplane(R.drawable.thumb_floatplane),
        Friendica(R.drawable.thumb_friendica),
        Fritz(R.drawable.thumb_fritz),
        Gamepad(R.drawable.thumb_gamepad),
        Gandi(R.drawable.thumb_gandi),
        Git(R.drawable.thumb_git),
        Gitea(R.drawable.thumb_gitea),
        GitHub(R.drawable.thumb_github),
        GitLab(R.drawable.thumb_gitlab),
        GMX(R.drawable.thumb_gmx),
        GoDaddy(R.drawable.thumb_godaddy),
        Gogs(R.drawable.thumb_gogs),
        Google(R.drawable.thumb_google),
        GovUK(R.drawable.thumb_govuk),
        Greenhost(R.drawable.thumb_greenhost),
        HackerOne(R.drawable.thumb_hackerone),
        Heroku(R.drawable.thumb_heroku),
        Hetzner(R.drawable.thumb_hetzner),
        HitBTC(R.drawable.thumb_hitbtc),
        HMRC(R.drawable.thumb_hmrc),
        HomeAssistant(R.drawable.thumb_home_assistant),
        Hover(R.drawable.thumb_hover),
        HumbleBundle(R.drawable.thumb_humblebundle),
        HurricaneElectric(R.drawable.thumb_hurricane_electric),
        IBM(R.drawable.thumb_ibm),
        Iconomi(R.drawable.thumb_iconomi),
        IFTTT(R.drawable.thumb_ifttt),
        Instagram(R.drawable.thumb_instagram),
        INWX(R.drawable.thumb_inwx),
        Itchio(R.drawable.thumb_itchio),
        Jagex(R.drawable.thumb_jagex),
        JetBrains(R.drawable.thumb_jetbrains),
        Joomla(R.drawable.thumb_joomla),
        Keeper(R.drawable.thumb_keeper),
        Kickstarter(R.drawable.thumb_kickstarter),
        Kraken(R.drawable.thumb_kraken),
        Kucoin(R.drawable.thumb_kucoin),
        LastPass(R.drawable.thumb_lastpass),
        LibreNMS(R.drawable.thumb_librenms),
        Lichess(R.drawable.thumb_lichess),
        LinkedIn(R.drawable.thumb_linkedin),
        Linode(R.drawable.thumb_linode),
        Liqui(R.drawable.thumb_liqui),
        LivelyMe(R.drawable.thumb_livelyme),
        Lobsters(R.drawable.thumb_lobsters),
        LocalBitcoins(R.drawable.thumb_localbitcoins),
        LocalMonero(R.drawable.thumb_localmonero),
        LoginGov(R.drawable.thumb_login_gov),
        LogMeIn(R.drawable.thumb_logmein),
        Mailbox(R.drawable.thumb_mailbox),
        Mailchimp(R.drawable.thumb_mailchimp),
        Mailcow(R.drawable.thumb_mailcow),
        Mailgun(R.drawable.thumb_mailgun),
        Mailru(R.drawable.thumb_mailru),
        Mapbox(R.drawable.thumb_mapbox),
        Mastodon(R.drawable.thumb_mastodon),
        Matomo(R.drawable.thumb_matomo),
        Mediawiki(R.mipmap.thumb_mediawiki, AssetType.Bitmap),
        Mega(R.drawable.thumb_mega),
        Microsoft(R.drawable.thumb_microsoft),
        MicrosoftTeams(R.drawable.thumb_microsoft_teams),
        Migadu(R.drawable.thumb_migadu),
        Mint(R.drawable.thumb_mint),
        Miraheze(R.drawable.thumb_miraheze),
        Mixer(R.drawable.thumb_mixer),
        MongoDB(R.drawable.thumb_mongodb),
        MVPSnet(R.drawable.thumb_mvpsnet),
        NameCheap(R.drawable.thumb_namecheap),
        NameCom(R.drawable.thumb_namecom),
        NAS(R.drawable.thumb_nas),
        netcup(R.drawable.thumb_netcup),
        NextCloud(R.drawable.thumb_nextcloud),
        Nintendo(R.drawable.thumb_nintendo),
        NoStarchPress(R.drawable.thumb_no_starch_press),
        NPM(R.drawable.thumb_npm),
        Oculus(R.drawable.thumb_oculus),
        Office(R.drawable.thumb_office),
        Okta(R.drawable.thumb_okta),
        OnlineNet(R.drawable.thumb_online),
        OpenVZ(R.drawable.thumb_openvz),
        OPNsense(R.drawable.thumb_opnsense),
        ORCiD(R.drawable.thumb_orcid),
        Origin(R.drawable.thumb_origin),
        OVH(R.drawable.thumb_ovh),
        Packet(R.drawable.thumb_packet),
        Parsecgaming(R.drawable.thumb_parsecgaming),
        Passwordstate(R.drawable.thumb_passwordstate),
        Patreon(R.drawable.thumb_patreon),
        PayPal(R.drawable.thumb_paypal),
        PaySafe(R.drawable.thumb_paysafecard),
        PayWithPrivacy(R.drawable.thumb_paywithprivacy),
        PCloud(R.drawable.thumb_pcloud),
        Phabricator(R.drawable.thumb_phabricator),
        phpMyAdmin(R.drawable.thumb_phpmyadmin),
        Plurk(R.drawable.thumb_plurk),
        Posteo(R.drawable.thumb_posteo),
        Pretix(R.drawable.thumb_pretix),
        Prey(R.drawable.thumb_prey),
        PrivateInternetAccess(R.drawable.thumb_private_internet_access),
        ProtonMail(R.drawable.thumb_protonmail),
        Proxmox(R.drawable.thumb_proxmox),
        Pushover(R.drawable.thumb_pushover),
        PyPI(R.drawable.thumb_pypi),
        PythonAnywhere(R.drawable.thumb_python_anywhere),
        Rackspace(R.drawable.thumb_rackspace),
        Reddit(R.drawable.thumb_reddit),
        RipeNNC(R.drawable.thumb_ripe_ncc),
        Robinhood(R.drawable.thumb_robinhood),
        Rockstar(R.drawable.thumb_rockstar),
        RSS(R.drawable.thumb_rss),
        SAP(R.drawable.thumb_sap),
        Scaleway(R.drawable.thumb_scaleway),
        School(R.drawable.thumb_school),
        Sciebo(R.drawable.thumb_sciebo),
        Seafile(R.mipmap.thumb_seafile, AssetType.Bitmap),
        Sentry(R.drawable.thumb_sentry),
        Sevdesk(R.drawable.thumb_sevdesk),
        Skrill(R.drawable.thumb_skrill),
        Slack(R.drawable.thumb_slack),
        Snapchat(R.drawable.thumb_snapchat),
        Sophos(R.drawable.thumb_sophos),
        SourceForge(R.drawable.thumb_sourceforge),
        Squarespace(R.drawable.thumb_squarespace),
        StandardNotes(R.drawable.thumb_standardnotes),
        StarCitizen(R.drawable.thumb_starcitizen),
        Steam(R.drawable.thumb_steam),
        Stripe(R.drawable.thumb_stripe),
        Sync(R.drawable.thumb_sync),
        Synology(R.drawable.thumb_synology),
        TeaHub(R.drawable.thumb_teahub),
        TeamViewer(R.drawable.thumb_teamviewer),
        Terminal(R.drawable.thumb_terminal),
        TransIP(R.drawable.thumb_transip),
        Trello(R.drawable.thumb_trello),
        Tumblr(R.drawable.thumb_tumblr),
        TurboTax(R.drawable.thumb_turbotax),
        Tutanota(R.drawable.thumb_tutanota),
        TUWien(R.drawable.thumb_tuwien_ac_at),
        Twilio(R.drawable.thumb_twilio),
        Twitch(R.drawable.thumb_twitch),
        Twitter(R.drawable.thumb_twitter),
        Uber(R.drawable.thumb_uber),
        UbiquitiNetworks(R.drawable.thumb_ubnt),
        Ubisoft(R.drawable.thumb_ubisoft),
        UbuntuOne(R.drawable.thumb_ubuntu_one),
        Uphold(R.drawable.thumb_uphold),
        USAA(R.drawable.thumb_usaa),
        VagrantCloud(R.drawable.thumb_vagrant_cloud),
        VEXXHOST(R.drawable.thumb_vexxhost),
        VK(R.drawable.thumb_vk),
        Vultr(R.drawable.thumb_vultr),
        Wallabag(R.drawable.thumb_wallabag),
        Wallet(R.drawable.thumb_wallet),
        Wargaming(R.drawable.thumb_wargaming),
        Wasabi(R.drawable.thumb_wasabi),
        WebDe(R.drawable.thumb_web_de),
        Wikimedia(R.drawable.thumb_wikimedia),
        Wordpress(R.drawable.thumb_wordpress),
        Workplace(R.drawable.thumb_workplace),
        Xero(R.drawable.thumb_xero),
        Xerox(R.drawable.thumb_xerox),
        Xing(R.drawable.thumb_xing),
        YandexMoney(R.drawable.thumb_yandex_money),
        Zapier(R.drawable.thumb_zapier),
        Zendesk(R.drawable.thumb_zendesk),
        Zoho(R.drawable.thumb_zoho),
        Zoom(R.drawable.thumb_zoom);

        private int resource;
        private AssetType assetType;

        EntryThumbnails(int resource) {
            this.resource = resource;
            this.assetType = AssetType.Vector;
        }

        EntryThumbnails(int resource, AssetType assetType) {
            this.resource = resource;
            this.assetType = assetType;
        }

        public int getResource() {
            return resource;
        }
        public AssetType getAssetType() {
            return assetType;
        }

        public static EntryThumbnails valueOfIgnoreCase(String thumbnail) {
            for (EntryThumbnails entryThumbnails : values())
                if (entryThumbnails.name().equalsIgnoreCase(thumbnail)) return entryThumbnails;
                throw new IllegalArgumentException();
        }
    }

    public static Bitmap getThumbnailGraphic(Context context, String issuer, String label, int size, EntryThumbnails thumbnail) {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        if (thumbnail == EntryThumbnails.Default && size > 0) {
            LetterBitmap letterBitmap = new LetterBitmap(context);
            String letterSrc = issuer.isEmpty() ? label : issuer;
            return letterBitmap.getLetterTile(letterSrc, letterSrc, size, size);
        } else if (thumbnail != EntryThumbnails.Default) {

            try {
                if (thumbnail.getAssetType() == AssetType.Vector) {
                    Drawable drawable = AppCompatResources.getDrawable(context, thumbnail.getResource());
                    Bitmap bitmap = Bitmap.createBitmap(drawable.getMinimumWidth(), drawable.getMinimumHeight(), Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(bitmap);
                    drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
                    drawable.draw(canvas);
                    return bitmap;
                } else {
                    return BitmapFactory.decodeResource(context.getResources(), thumbnail.getResource());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher_round);
    }
}
