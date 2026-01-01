class AutoSuggest {
	constructor(options) {
		this.input = $(options.input);
		this.resultsBox = $(options.resultsBox);
		this.api = options.api; // function(query) → URL
		this.onSelect = options.onSelect; // callback(selectedItem)
		this.delay = options.delay || 300;

		this.suggestions = [];
		this.selectedIndex = -1;
		this.delayTimer = null;

		this.contentType = options.contentType || "application/json";
		this.headerKey = options.headerKey || "jwt";
		   this.headerValue = options.headerValue || "";

		this.attachEvents();
	}

	attachEvents() {
		// Input event
		this.input.on("input", () => this.handleInput());

		// Keyboard navigation
		this.input.on("keydown", (e) => this.handleKeyDown(e));

		// Click outside hides box
		$(document).on("click", (e) => {
			if (!$(e.target).closest(this.resultsBox).length &&
				!$(e.target).is(this.input)) {
				this.resultsBox.addClass("hidden");
			}
		});
	}

	handleInput() {
		let query = this.input.val().trim();

		if (this.delayTimer) clearTimeout(this.delayTimer);

		if (query.length < 2) {
			this.resultsBox.addClass("hidden");
			return;
		}

		this.delayTimer = setTimeout(() => {
			$.ajax({
				url: this.api(query),
				method: "GET",
				headers: {
					[this.headerKey]: this.headerValue,
					"Content-Type": this.contentType
				},
				success: (data) => this.showSuggestions(data)
			});
		}, this.delay);
	}

	showSuggestions(list) {
		this.resultsBox.empty();
		this.suggestions = list || [];
		this.selectedIndex = -1;

		if (!list || list.length === 0) {
			this.resultsBox.append(`
                <div class="p-2 text-gray-600">No Results Found</div>
            `);
		} else {
			list.forEach((item, index) => {
				this.resultsBox.append(`
                    <div class="suggest-item p-2 cursor-pointer hover:bg-gray-100"
                         data-index="${index}">
                        ${item.fullName}<br>
                        <sup style="color:gray;">ph:${item.mobile}, fam:${item.familyName}</sup>
                    </div>
                `);
			});

			// Click select
			this.resultsBox.find(".suggest-item").on("click", (e) => {
				let idx = $(e.currentTarget).data("index");
				this.selectItem(this.suggestions[idx]);
			});
		}

		this.resultsBox.removeClass("hidden");
	}

	handleKeyDown(e) {
		let items = this.resultsBox.find(".suggest-item");
		if (items.length === 0) return;

		if (e.key === "ArrowDown") {
			e.preventDefault();
			this.selectedIndex = (this.selectedIndex + 1) % items.length;
			this.highlight(items);
		}

		if (e.key === "ArrowUp") {
			e.preventDefault();
			this.selectedIndex = (this.selectedIndex - 1 + items.length) % items.length;
			this.highlight(items);
		}

		if (e.key === "Enter") {
			e.preventDefault();
			if (this.selectedIndex >= 0 && this.suggestions[this.selectedIndex]) {
				this.selectItem(this.suggestions[this.selectedIndex]);
			}
		}

		if (e.key === "Escape") {
			this.resultsBox.addClass("hidden");
		}
	}

	highlight(items) {
		items.removeClass("suggestion-active");
		$(items[this.selectedIndex]).addClass("suggestion-active");
	}

	selectItem(item) {
		this.input.val(item.fullName);
		this.resultsBox.addClass("hidden");
		if (this.onSelect) this.onSelect(item);
	}
}
