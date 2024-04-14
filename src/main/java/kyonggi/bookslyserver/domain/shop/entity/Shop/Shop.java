package kyonggi.bookslyserver.domain.shop.entity.Shop;

import jakarta.persistence.*;
import kyonggi.bookslyserver.domain.reservation.entity.ReservationSchedule;
import kyonggi.bookslyserver.domain.reservation.entity.ReservationSetting;
import kyonggi.bookslyserver.domain.review.entity.Review;
import kyonggi.bookslyserver.domain.shop.constant.CategoryName;
import kyonggi.bookslyserver.domain.shop.entity.BusinessSchedule.BusinessSchedule;
import kyonggi.bookslyserver.domain.shop.entity.Menu.Menu;
import kyonggi.bookslyserver.domain.user.entity.ShopOwner;
import kyonggi.bookslyserver.global.common.BaseTimeEntity;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@ToString(of = {"name", "store_number", "description"})
public class Shop extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private int totalVisitors;

    private int todayVisitors;

    private String phoneNumber;

    private String instagramUrl;

    private String kakaoUrl;

    @Lob
    private String introduction;

    private String detailAddress;

    private boolean isClosed;

    private String zipCode;

    private String streetAddress;

    private String businessNumber;

    @Embedded
    private TimeUnit timeUnit;

    @ManyToOne(fetch = LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "shopOwner_id")
    private ShopOwner shopOwner;

    @ManyToOne(fetch = LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "address_id")
    private Address address;

    @OneToMany(mappedBy = "shop", cascade = CascadeType.ALL)
    private List<ShopImage> shopImages = new ArrayList<>();

    @OneToMany(mappedBy = "shop", cascade = CascadeType.ALL)
    private List<Review> reviews = new ArrayList<>();

    @OneToMany(mappedBy = "shop", cascade = CascadeType.ALL)
    private List<BusinessSchedule> businessSchedules = new ArrayList<>();

    @OneToMany(mappedBy = "shop", cascade = CascadeType.ALL)
    private List<Menu> menus = new ArrayList<>();

    @OneToMany(mappedBy = "shop", cascade = CascadeType.ALL)
    private List<ReservationSchedule> reservationSchedules = new ArrayList<>();

    public void getBusinessSchedule(BusinessSchedule businessSchedule){
        this.businessSchedules.add(businessSchedule);
        businessSchedule.setShop(this);
    }

    public void getShopImage(ShopImage shopImage){
        this.shopImages.add(shopImage);
        shopImage.setShop(this);
    }



}
