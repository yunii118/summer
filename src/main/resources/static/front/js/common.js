/**
* 상품 상세 기능 모음
*
*/
const productDetails = {
    /**
    * 구매 수량 변경
    *
    */
    changeEa(e) {
        /* 표기 수량 변경*/
            const targetEl = document.getElementById(this.dataset.targetId);
            const inputEl = document.getElementById(this.dataset.eaId);
            const price = Number(this.dataset.price);

            let ea = Number(inputEl.value);
            let i = 0;
            const classList = this.classList;
            if (classList.contains('down')) {
                ea--;
                if(ea != 0){
                    i--;
                }
            } else {
                ea++;
                i++;
            }

            ea = ea < 1 ? 1 : ea;
            inputEl.value = ea;

            const total = price * ea;
            targetEl.innerText = total.toLocaleString();

          /* 총 상품 금액 변경 */
          const totalPrice = document.getElementById(this.dataset.totalPrice);
          const salePrice = document.getElementById(this.dataset.salePrice).innerText.replace(/,/g, "");
          const deliveryPrice = document.getElementById("delivery_price");
          const discount = document.getElementById(this.dataset.discount);
          const discountType = document.getElementById(this.dataset.discountType);
          let lastPrice = 0;
          if(discount.innerText != '0'){
              if(discountType.innerText == '%'){
                lastPrice = Number(salePrice) - (Number(salePrice) * Number(discount.innerText.replace(/\,/g, '')) * 0.01);
              }else{

                lastPrice = Number(salePrice) - Number(discount.innerText.replace(/\,/g, ''));
              }
          }else{
            lastPrice = Number(salePrice);
          }

          if(deliveryPrice != null){
              const el = deliveryPrice.innerText.replace(/,/g, "") * 1;
              totalPrice.innerText = (Number(lastPrice) * ea + el).toLocaleString();
          }else{
              totalPrice.innerText = (Number(lastPrice) * ea).toLocaleString();
          }


          const ordTotalPrice = document.querySelector("#ordTotalPrice");
          const payPrice = document.querySelector("#ordPayPrice");
          const ordDeliveryPrice = document.querySelector("#ordTotalDeliveryPrice").innerText;
          const totalDiscount = document.querySelector("#ordTotalDiscount");

          if(ordTotalPrice != null){
              totalDiscount.innerText = (Number(totalDiscount.innerText.replace(/,/g, '')) + i * (Number(salePrice) - Number(lastPrice))).toLocaleString();
              ordTotalPrice.innerText = (Number(ordTotalPrice.innerText.replace(/,/g, '')) + Number(i) * price).toLocaleString();
              payPrice.innerText = (Number(ordTotalPrice.innerText.replace(/,/g, '')) + Number(ordDeliveryPrice.replace(/,/g, '')) - Number(totalDiscount.innerText.replace(/,/g, ''))).toLocaleString();

          }
    }
};

// 슬라이크 전체 크기(width 구하기)
document.addEventListener('DOMContentLoaded', function() {
const slide = document.querySelector(".slide");
let slideWidth = slide.clientWidth;

// 버튼 엘리먼트 선택하기
const prevBtn = document.querySelector(".slide_prev_button");
const nextBtn = document.querySelector(".slide_next_button");

// 슬라이드 전체를 선택해 값을 변경해주기 위해 슬라이드 전체 선택하기
let slideItems = document.querySelectorAll(".slide_item");
// 현재 슬라이드 위치가 슬라이드 개수를 넘기지 않게 하기 위한 변수
const maxSlide = slideItems.length;

// 버튼 클릭할 때 마다 현재 슬라이드가 어디인지 알려주기 위한 변수
let currSlide = 1;

// 페이지네이션 생성
const pagination = document.querySelector(".slide_pagination");

for (let i = 0; i < maxSlide; i++) {
  if (i === 0) pagination.innerHTML += `<li class="active">•</li>`;
  else pagination.innerHTML += `<li>•</li>`;
}

const paginationItems = document.querySelectorAll(".slide_pagination > li");

// 무한 슬라이드를 위해 start, end 슬라이드 복사하기
const startSlide = slideItems[0];
const endSlide = slideItems[slideItems.length - 1];
const startElem = document.createElement("div");
const endElem = document.createElement("div");

if (endSlide) {
    endSlide.classList.forEach((c) => endElem.classList.add(c));
    endElem.innerHTML = endSlide.innerHTML;
}

if (startSlide) {
    startSlide.classList.forEach((c) => startElem.classList.add(c));
    startElem.innerHTML = startSlide.innerHTML;
}

if (slideItems && slideItems.length > 0) {
    // 각 복제한 엘리먼트 추가하기
    slideItems[0].before(endElem);
    slideItems[slideItems.length - 1].after(startElem);

    // 슬라이드 전체를 선택해 값을 변경해주기 위해 슬라이드 전체 선택하기
    slideItems = document.querySelectorAll(".slide_item");
    //
    let offset = slideWidth + currSlide;
    slideItems.forEach((i) => {
      i.setAttribute("style", `left: ${-offset}px`);
    });
}
function nextMove() {
  currSlide++;
  // 마지막 슬라이드 이상으로 넘어가지 않게 하기 위해서
  if (currSlide <= maxSlide) {
    // 슬라이드를 이동시키기 위한 offset 계산
    const offset = slideWidth * currSlide;
    // 각 슬라이드 아이템의 left에 offset 적용
    slideItems.forEach((i) => {
      i.setAttribute("style", `left: ${-offset}px`);
    });
    // 슬라이드 이동 시 현재 활성화된 pagination 변경
    paginationItems.forEach((i) => i.classList.remove("active"));
    paginationItems[currSlide - 1].classList.add("active");
  } else {
    // 무한 슬라이드 기능 - currSlide 값만 변경해줘도 되지만 시각적으로 자연스럽게 하기 위해 아래 코드 작성
    currSlide = 0;
    let offset = slideWidth * currSlide;
    slideItems.forEach((i) => {
      i.setAttribute("style", `transition: ${0}s; left: ${-offset}px`);
    });
    currSlide++;
    offset = slideWidth * currSlide;
    // 각 슬라이드 아이템의 left에 offset 적용
    setTimeout(() => {
      // 각 슬라이드 아이템의 left에 offset 적용
      slideItems.forEach((i) => {
        // i.setAttribute("style", `transition: ${0}s; left: ${-offset}px`);
        i.setAttribute("style", `transition: ${0.5}s; left: ${-offset}px`);
      });
    }, 0);
    // // 슬라이드 이동 시 현재 활성화된 pagination 변경
    paginationItems.forEach((i) => i.classList.remove("active"));
    paginationItems[currSlide - 1].classList.add("active");
  }
}
function prevMove() {
  currSlide--;
  // 1번째 슬라이드 이하로 넘어가지 않게 하기 위해서
  if (currSlide > 0) {
    // 슬라이드를 이동시키기 위한 offset 계산
    const offset = slideWidth * currSlide;
    // 각 슬라이드 아이템의 left에 offset 적용
    slideItems.forEach((i) => {
      i.setAttribute("style", `left: ${-offset}px`);
    });
    // 슬라이드 이동 시 현재 활성화된 pagination 변경
    paginationItems.forEach((i) => i.classList.remove("active"));
    paginationItems[currSlide - 1].classList.add("active");
  } else {
    // 무한 슬라이드 기능 - currSlide 값만 변경해줘도 되지만 시각적으로 자연스럽게 하기 위해 아래 코드 작성
    currSlide = maxSlide + 1;
    let offset = slideWidth * currSlide;
    // 각 슬라이드 아이템의 left에 offset 적용
    slideItems.forEach((i) => {
      i.setAttribute("style", `transition: ${0}s; left: ${-offset}px`);
    });
    currSlide--;
    offset = slideWidth * currSlide;
    setTimeout(() => {
      // 각 슬라이드 아이템의 left에 offset 적용
      slideItems.forEach((i) => {
        // i.setAttribute("style", `transition: ${0}s; left: ${-offset}px`);
        i.setAttribute("style", `transition: ${0.15}s; left: ${-offset}px`);
      });
    }, 0);
    // 슬라이드 이동 시 현재 활성화된 pagination 변경
    paginationItems.forEach((i) => i.classList.remove("active"));
    paginationItems[currSlide - 1].classList.add("active");
  }
}

// 버튼 엘리먼트에 클릭 이벤트 추가하기
if (nextBtn) {
    nextBtn.addEventListener("click", () => {
      // 이후 버튼 누를 경우 현재 슬라이드를 변경
      nextMove();
    });
}
if (prevBtn) {
    // 버튼 엘리먼트에 클릭 이벤트 추가하기
    prevBtn.addEventListener("click", () => {
      // 이전 버튼 누를 경우 현재 슬라이드를 변경
      prevMove();
    });
}
// 브라우저 화면이 조정될 때 마다 slideWidth를 변경하기 위해
window.addEventListener("resize", () => {
  slideWidth = slide.clientWidth;
});

if (paginationItems && paginationItems.length > 0) {
// 각 페이지네이션 클릭 시 해당 슬라이드로 이동하기
    for (let i = 0; i < maxSlide; i++) {
      // 각 페이지네이션마다 클릭 이벤트 추가하기
      paginationItems[i].addEventListener("click", () => {
        // 클릭한 페이지네이션에 따라 현재 슬라이드 변경해주기(currSlide는 시작 위치가 1이기 때문에 + 1)
        currSlide = i + 1;
        // 슬라이드를 이동시키기 위한 offset 계산
        const offset = slideWidth * currSlide;
        // 각 슬라이드 아이템의 left에 offset 적용
        slideItems.forEach((i) => {
          i.setAttribute("style", `left: ${-offset}px`);
        });
        // 슬라이드 이동 시 현재 활성화된 pagination 변경
        paginationItems.forEach((i) => i.classList.remove("active"));
        paginationItems[currSlide - 1].classList.add("active");
      });
    }
}
// 드래그(스와이프) 이벤트를 위한 변수 초기화
let startPoint = 0;
let endPoint = 0;

if (slide) {
    // PC 클릭 이벤트 (드래그)
    slide.addEventListener("mousedown", (e) => {
      startPoint = e.pageX; // 마우스 드래그 시작 위치 저장
    });

    slide.addEventListener("mouseup", (e) => {
      endPoint = e.pageX; // 마우스 드래그 끝 위치 저장
      if (startPoint < endPoint) {
        // 마우스가 오른쪽으로 드래그 된 경우
        prevMove();
      } else if (startPoint > endPoint) {
        // 마우스가 왼쪽으로 드래그 된 경우
        nextMove();
      }
    });

    // 모바일 터치 이벤트 (스와이프)
    slide.addEventListener("touchstart", (e) => {
      startPoint = e.touches[0].pageX; // 터치가 시작되는 위치 저장
    });
    slide.addEventListener("touchend", (e) => {
      endPoint = e.changedTouches[0].pageX; // 터치가 끝나는 위치 저장
      if (startPoint < endPoint) {
        // 오른쪽으로 스와이프 된 경우
        prevMove();
      } else if (startPoint > endPoint) {
        // 왼쪽으로 스와이프 된 경우
        nextMove();
      }
    });

    // 기본적으로 슬라이드 루프 시작하기
    let loopInterval = setInterval(() => {
      nextMove();
    }, 5000);

    // 슬라이드에 마우스가 올라간 경우 루프 멈추기
    slide.addEventListener("mouseover", () => {
      clearInterval(loopInterval);
    });

    // 슬라이드에서 마우스가 나온 경우 루프 재시작하기
    slide.addEventListener("mouseout", () => {
      loopInterval = setInterval(() => {
        nextMove();
      }, 5000);
    });
}
window.addEventListener("DOMContentLoaded", function() {
    /* 전체 선택 토글 기능 S */
    const checkAlls = document.getElementsByClassName("checkall");
    for(const el of checkAlls){
        el.addEventListener("click", function(){
            const targetName = this.dataset.targetName;
            const chks = document.getElementsByName(targetName);
            for(const el of chks){
                el.checked =this.checked;
            }
        });
    }
    /* 전체 선택 토글 기능 E */

});

});




