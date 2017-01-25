package com.github.kongchen.swagger.docgen.mavenplugin;

import com.wordnik.swagger.model.ApiListingReference;

import java.util.Comparator;

/**
 * Created by grzegorz.dyk on 10/01/17.
 */
class ApiListingReferenceComparator implements Comparator<ApiListingReference> {
  @Override
  public int compare(ApiListingReference o1, ApiListingReference o2) {
    if (o1 == null && o2 == null) return 0;
    if (o1 == null) return -1;
    if (o2 == null) return 1;
    return o1.position() - o2.position();
  }
}
