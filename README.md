# HTML-preprocesor
Preprocessor for HTML files that includes all CSS style sheets, It also includes Script tags without the async attribute and utilizes the W3C library attribute 'w3-include-html' to include child nodes. this was done to decrease the number of round trips and increase page load times, when compressing(gzip) files this also results in smaller overall file sizes decreasing total data usage..