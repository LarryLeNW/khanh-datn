
            <div th:fragment="search" class="rental-container">
              <!-- Form 1: Tiêu đề và buttons -->
              <div class="rental-form">
                
                <div class="form-group">
                  <div class="vehicle-type">
                    <button class="btn btn-select active" data-type="car">
                      Ô tô <i class="fas fa-check"></i>
                    </button>
                    <button class="btn btn-select" data-type="bike" >
                      Xe máy <i class="fas fa-check"></i>
                    </button>
                  </div>
                </div>
              </div>

              <!-- Form 2: Thông tin tìm kiếm -->
              <div class="rental-form-search">
                <div class="form-row">
                  <div class="form-group">
                    <label for="days">Địa Điểm</label>
                    <select id="location">
                      <option>TP. Hồ Chí Minh</option>
                      <option>Hà Nội</option>
                      <option>Đà Nẵng</option>
                    </select>
                  </div>
                  <div class="form-group date-input">
                    <label for="pickup-date" >Ngày nhận xe</label>
                    <input type="datetime-local" id="pickup-date" value="2024-11-21T07:00">
                  </div>
                  <div class="form-group date-input">
                    <label for="return-date">Ngày trả xe</label>
                    <input type="datetime-local" id="return-date" value="2024-11-21T19:00">
                  </div>
                  <div class="form-group btn-group">
                    <button class="btn btn-primary">Tìm xe</button>
                  </div>
                </div>
              </div>
            
            </div>