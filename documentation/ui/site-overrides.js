;(function () {
  function makeImagesClickable() {
    var images = document.querySelectorAll('.doc .imageblock img')
    images.forEach(function (img) {
      if (img.closest('a')) return
      var src = img.getAttribute('src')
      if (!src) return
      var link = document.createElement('a')
      link.className = 'nf-bids-fullres-link'
      link.href = src
      link.target = '_blank'
      link.rel = 'noopener noreferrer'
      img.parentNode.insertBefore(link, img)
      link.appendChild(img)
    })
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', makeImagesClickable)
  } else {
    makeImagesClickable()
  }
})()
